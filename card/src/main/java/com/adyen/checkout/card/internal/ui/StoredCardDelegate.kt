/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardInputData
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class StoredCardDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: CardComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val cardEncryptor: BaseCardEncryptor,
    private val publicKeyRepository: PublicKeyRepository,
    private val submitHandler: SubmitHandler<CardComponentState>,
) : CardDelegate {

    private val noCvcBrands: Set<CardBrand> = hashSetOf(CardBrand(cardType = CardType.BCMC))

    private val cardType = CardBrand(txVariant = storedPaymentMethod.brand.orEmpty())
    private val storedDetectedCardTypes = DetectedCardType(
        cardType,
        isReliable = true,
        enableLuhnCheck = true,
        cvcPolicy = when {
            componentParams.storedCVCVisibility == StoredCVCVisibility.HIDE ||
                noCvcBrands.contains(cardType) -> Brand.FieldPolicy.HIDDEN

            else -> Brand.FieldPolicy.REQUIRED
        },
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        panLength = null,
        paymentMethodVariant = null,
    )

    private val inputData: CardInputData = CardInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<CardOutputData> = _outputDataFlow

    override val addressOutputData: AddressOutputData
        get() = _outputDataFlow.value.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData>
        get() = MutableStateFlow(_outputDataFlow.value.addressState)

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<CardComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<CardComponentState> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override val outputData: CardOutputData get() = _outputDataFlow.value

    private var publicKey: String? = null

    private var coroutineScope: CoroutineScope? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope

        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)
        initializeInputData()
        fetchPublicKey()

        val requiresShopperInput = !isCvcHidden()
        if (requiresShopperInput) {
            _viewFlow.tryEmit(CardComponentViewType.StoredCardView)
        } else {
            // trigger submission as soon as state is ready
            componentStateFlow.onEach { onState(it) }.launchIn(coroutineScope)
        }
    }

    private fun onState(cardComponentState: CardComponentState) {
        if (cardComponentState.isValid) {
            submitHandler.onSubmit(cardComponentState)
        }
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = storedPaymentMethod.type.orEmpty(),
            isStoredPaymentMethod = true,
        )
        analyticsManager.trackEvent(event)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<CardComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = exceptionFlow,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun fetchPublicKey() {
        coroutineScope?.launch {
            publicKeyRepository.fetchPublicKey(
                environment = componentParams.environment,
                clientKey = componentParams.clientKey,
            ).fold(
                onSuccess = { key ->
                    publicKey = key
                    updateComponentState(outputData)
                },
                onFailure = { e ->
                    exceptionChannel.trySend(ComponentException("Unable to fetch publicKey.", e))
                },
            )
        }
    }

    override fun updateInputData(update: CardInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    private fun onInputDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onInputDataChanged" }

        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = with(inputData) {
        CardOutputData(
            cardNumberState = FieldState(cardNumber, Validation.Valid),
            expiryDateState = FieldState(expiryDate, Validation.Valid),
            securityCodeState = validateSecurityCode(securityCode, storedDetectedCardTypes),
            holderNameState = FieldState(holderName, Validation.Valid),
            socialSecurityNumberState = FieldState(socialSecurityNumber, Validation.Valid),
            kcpBirthDateOrTaxNumberState = FieldState(kcpBirthDateOrTaxNumber, Validation.Valid),
            kcpCardPasswordState = FieldState(kcpCardPassword, Validation.Valid),
            addressState = AddressValidationUtils.makeValidEmptyAddressOutput(inputData.address),
            installmentState = FieldState(inputData.installmentOption, Validation.Valid),
            shouldStorePaymentMethod = isStorePaymentMethodSwitchChecked,
            cvcUIState = makeCvcUIState(storedDetectedCardTypes.cvcPolicy),
            expiryDateUIState = makeExpiryDateUIState(storedDetectedCardTypes.expiryDatePolicy),
            holderNameUIState = InputFieldUIState.HIDDEN,
            showStorePaymentField = false,
            detectedCardTypes = listOf(storedDetectedCardTypes),
            isSocialSecurityNumberRequired = false,
            isKCPAuthRequired = false,
            addressUIState = AddressFormUIState.NONE,
            installmentOptions = emptyList(),
            cardBrands = emptyList(),
            isDualBranded = false,
            kcpBirthDateOrTaxNumberHint = null,
            isCardListVisible = false,
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: CardOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount")
    private fun createComponentState(
        outputData: CardOutputData = this.outputData
    ): CardComponentState {
        val cardNumber = outputData.cardNumberState.value

        val firstCardBrand = outputData.detectedCardTypes.firstOrNull()?.cardBrand

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid || publicKey == null) {
            return CardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = outputData.isValid,
                isReady = publicKey != null,
                cardBrand = firstCardBrand,
                binValue = "",
                lastFourDigits = null,
            )
        }

        val unencryptedCardBuilder = UnencryptedCard.Builder()

        val encryptedCard: EncryptedCard = try {
            if (!isCvcHidden()) {
                val cvc = outputData.securityCodeState.value
                if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
            }
            val expiryDateResult = outputData.expiryDateState.value
            if (expiryDateResult != ExpiryDate.EMPTY_DATE) {
                unencryptedCardBuilder.setExpiryDate(
                    expiryMonth = expiryDateResult.expiryMonth.toString(),
                    expiryYear = expiryDateResult.expiryYear.toString(),
                )
            }

            cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            exceptionChannel.trySend(e)
            return CardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = false,
                isReady = true,
                cardBrand = firstCardBrand,
                binValue = "",
                lastFourDigits = null,
            )
        }

        return mapComponentState(
            encryptedCard,
            cardNumber,
            firstCardBrand,
        )
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(storedPaymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    override fun startAddressLookup() = Unit

    override fun handleBackPress(): Boolean {
        return false
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    private fun validateSecurityCode(securityCode: String, detectedCardType: DetectedCardType): FieldState<String> {
        val cvcUiState = makeCvcUIState(detectedCardType.cvcPolicy)
        return CardValidationUtils.validateSecurityCode(securityCode, detectedCardType, cvcUiState)
    }

    private fun isCvcHidden(): Boolean {
        return outputData.cvcUIState == InputFieldUIState.HIDDEN
    }

    private fun mapComponentState(
        encryptedCard: EncryptedCard,
        cardNumber: String,
        firstCardBrand: CardBrand?,
    ): CardComponentState {
        val cardPaymentMethod = CardPaymentMethod(
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
        ).apply {
            storedPaymentMethodId = getPaymentMethodId()

            if (!isCvcHidden()) {
                encryptedSecurityCode = encryptedCard.encryptedSecurityCode
            }

            threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
        }

        val paymentComponentData = makePaymentComponentData(cardPaymentMethod)

        val lastFour = cardNumber.takeLast(LAST_FOUR_LENGTH)

        return CardComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
            cardBrand = firstCardBrand,
            binValue = "",
            lastFourDigits = lastFour,
        )
    }

    private fun makePaymentComponentData(
        cardPaymentMethod: CardPaymentMethod
    ): PaymentComponentData<CardPaymentMethod> {
        return PaymentComponentData(
            paymentMethod = cardPaymentMethod,
            shopperReference = componentParams.shopperReference,
            order = order,
            amount = componentParams.amount,
        )
    }

    private fun initializeInputData() {
        inputData.cardNumber = storedPaymentMethod.lastFour.orEmpty()

        try {
            val storedDate = ExpiryDate(
                storedPaymentMethod.expiryMonth.orEmpty().toInt(),
                storedPaymentMethod.expiryYear.orEmpty().toInt(),
            )
            inputData.expiryDate = storedDate
        } catch (e: NumberFormatException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to parse stored Date" }
            inputData.expiryDate = ExpiryDate.EMPTY_DATE
        }

        onInputDataChanged()
    }

    private fun makeCvcUIState(cvcPolicy: Brand.FieldPolicy): InputFieldUIState {
        adyenLog(AdyenLogLevel.DEBUG) { "makeCvcUIState: $cvcPolicy" }
        return when (cvcPolicy) {
            Brand.FieldPolicy.REQUIRED -> InputFieldUIState.REQUIRED
            Brand.FieldPolicy.OPTIONAL -> InputFieldUIState.OPTIONAL
            Brand.FieldPolicy.HIDDEN -> InputFieldUIState.HIDDEN
        }
    }

    private fun makeExpiryDateUIState(expiryDatePolicy: Brand.FieldPolicy): InputFieldUIState {
        return when {
            !expiryDatePolicy.isRequired() -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun getPaymentMethodId(): String {
        return storedPaymentMethod.id ?: "ID_NOT_FOUND"
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        // no ops
    }

    // Bin doesn't change for stored cards
    override fun setOnBinValueListener(listener: ((binValue: String) -> Unit)?) = Unit

    // Bin lookup is not performed for stored cards
    override fun setOnBinLookupListener(listener: ((data: List<BinLookupData>) -> Unit)?) = Unit

    override fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback) = Unit

    override fun updateAddressLookupOptions(options: List<LookupAddress>) = Unit

    override fun setAddressLookupResult(addressLookupResult: AddressLookupResult) = Unit

    override fun onCleared() {
        removeObserver()
        coroutineScope = null
        analyticsManager.clear(this)
    }

    companion object {
        private const val LAST_FOUR_LENGTH = 4
    }
}
