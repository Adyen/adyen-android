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
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardInputData
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.ExpiryDate
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.CardAddressValidationUtils
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.card.internal.util.KcpValidationUtils
import com.adyen.checkout.card.internal.util.toBinLookupData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.isEmpty
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncrypter
import com.adyen.checkout.cse.internal.BaseGenericEncrypter
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.checkout.ui.core.internal.util.SocialSecurityNumberUtils
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
internal class DefaultCardDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val publicKeyRepository: PublicKeyRepository,
    override val componentParams: CardComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    private val analyticsRepository: AnalyticsRepository,
    private val addressRepository: AddressRepository,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncrypter: BaseCardEncrypter,
    private val genericEncrypter: BaseGenericEncrypter,
    private val submitHandler: SubmitHandler<CardComponentState>,
) : CardDelegate {

    private val inputData: CardInputData = CardInputData()

    private var publicKey: String? = null

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<CardOutputData> = _outputDataFlow

    override val addressOutputData: AddressOutputData
        get() = outputData.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        outputDataFlow.map {
            it.addressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, outputData.addressState)
    }

    override val outputData: CardOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<CardComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val _viewFlow: MutableStateFlow<ComponentViewType?> =
        MutableStateFlow(CardComponentViewType.DefaultCardView)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<CardComponentState> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var onBinValueListener: ((binValue: String) -> Unit)? = null
    private var onBinLookupListener: ((data: List<BinLookupData>) -> Unit)? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope

        submitHandler.initialize(coroutineScope, componentStateFlow)

        setupAnalytics(coroutineScope)
        fetchPublicKey()
        subscribeToDetectedCardTypes()

        if (componentParams.addressParams is AddressParams.FullAddress) {
            subscribeToStatesList()
            subscribeToCountryList()
            requestCountryList()
        }
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "setupAnalytics")
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
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
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun fetchPublicKey() {
        Logger.d(TAG, "fetchPublicKey")
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = componentParams.environment,
                clientKey = componentParams.clientKey
            ).fold(
                onSuccess = { key ->
                    Logger.d(TAG, "Public key fetched")
                    publicKey = key
                    updateComponentState(outputData)
                },
                onFailure = { e ->
                    Logger.e(TAG, "Unable to fetch public key")
                    exceptionChannel.trySend(ComponentException("Unable to fetch publicKey.", e))
                }
            )
        }
    }

    override fun updateInputData(update: CardInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        updateInputData {
            this.address.update()
        }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        detectCardTypeRepository.detectCardType(
            cardNumber = inputData.cardNumber,
            publicKey = publicKey,
            supportedCardBrands = componentParams.supportedCardBrands,
            clientKey = componentParams.clientKey,
            coroutineScope = coroutineScope
        )
        requestStateList(inputData.address.country)
    }

    private fun subscribeToDetectedCardTypes() {
        detectCardTypeRepository.detectedCardTypesFlow
            .onEach { detectedCardTypes ->
                Logger.d(
                    TAG,
                    "New detected card types emitted - detectedCardTypes: ${detectedCardTypes.map { it.cardBrand }} " +
                        "- isReliable: ${detectedCardTypes.firstOrNull()?.isReliable}"
                )
                if (outputData.detectedCardTypes != detectedCardTypes) {
                    onBinLookupListener?.invoke(detectedCardTypes.map(DetectedCardType::toBinLookupData))
                }
                updateOutputData(detectedCardTypes = detectedCardTypes)
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToCountryList() {
        addressRepository.countriesFlow
            .distinctUntilChanged()
            .onEach { countries ->
                Logger.d(TAG, "New countries emitted - countries: ${countries.size}")
                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    shopperLocale = componentParams.shopperLocale,
                    addressParams = componentParams.addressParams,
                    countryList = countries
                )
                countryOptions.firstOrNull { it.selected }?.let {
                    inputData.address.country = it.code
                    requestStateList(it.code)
                }
                updateOutputData(countryOptions = countryOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToStatesList() {
        addressRepository.statesFlow
            .distinctUntilChanged()
            .onEach { states ->
                Logger.d(TAG, "New states emitted - states: ${states.size}")
                updateOutputData(stateOptions = AddressFormUtils.initializeStateOptions(states))
            }
            .launchIn(coroutineScope)
    }

    private fun updateOutputData(
        detectedCardTypes: List<DetectedCardType> = outputData.detectedCardTypes,
        countryOptions: List<AddressListItem> = outputData.addressState.countryOptions,
        stateOptions: List<AddressListItem> = outputData.addressState.stateOptions,
    ) {
        val newOutputData = createOutputData(detectedCardTypes, countryOptions, stateOptions)
        _outputDataFlow.tryEmit(newOutputData)
        updateComponentState(newOutputData)
    }

    @Suppress("LongMethod")
    private fun createOutputData(
        detectedCardTypes: List<DetectedCardType> = emptyList(),
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
    ): CardOutputData {
        Logger.v(TAG, "createOutputData")
        val updatedCountryOptions = AddressFormUtils.markAddressListItemSelected(
            countryOptions,
            inputData.address.country
        )
        val updatedStateOptions = AddressFormUtils.markAddressListItemSelected(
            stateOptions,
            inputData.address.stateOrProvince
        )

        val isReliable = detectedCardTypes.any { it.isReliable }

        val filteredDetectedCardTypes = DetectedCardTypesUtils.filterDetectedCardTypes(
            detectedCardTypes,
            inputData.selectedCardIndex
        )
        val selectedOrFirstCardType = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
            detectedCardTypes = filteredDetectedCardTypes
        )

        val reliableSelectedCard = if (isReliable) selectedOrFirstCardType else null

        // perform a Luhn Check if no brands are detected
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true

        // when no supported cards are detected, only show an error if the brand detection was reliable
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val addressFormUIState = AddressFormUIState.fromAddressParams(componentParams.addressParams)

        return CardOutputData(
            cardNumberState = validateCardNumber(
                cardNumber = inputData.cardNumber,
                enableLuhnCheck = enableLuhnCheck,
                isBrandSupported = !shouldFailWithUnsupportedBrand
            ),
            expiryDateState = validateExpiryDate(inputData.expiryDate, selectedOrFirstCardType?.expiryDatePolicy),
            securityCodeState = validateSecurityCode(inputData.securityCode, selectedOrFirstCardType),
            holderNameState = validateHolderName(inputData.holderName),
            socialSecurityNumberState = validateSocialSecurityNumber(inputData.socialSecurityNumber),
            kcpBirthDateOrTaxNumberState = validateKcpBirthDateOrTaxNumber(inputData.kcpBirthDateOrTaxNumber),
            kcpCardPasswordState = validateKcpCardPassword(inputData.kcpCardPassword),
            addressState = validateAddress(
                inputData.address,
                addressFormUIState,
                reliableSelectedCard,
                updatedCountryOptions,
                updatedStateOptions
            ),
            installmentState = makeInstallmentFieldState(inputData.installmentOption),
            shouldStorePaymentMethod = inputData.isStorePaymentMethodSwitchChecked,
            cvcUIState = makeCvcUIState(selectedOrFirstCardType?.cvcPolicy),
            expiryDateUIState = makeExpiryDateUIState(selectedOrFirstCardType?.expiryDatePolicy),
            holderNameUIState = getHolderNameUIState(),
            showStorePaymentField = showStorePaymentField(),
            detectedCardTypes = filteredDetectedCardTypes,
            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired(),
            isKCPAuthRequired = isKCPAuthRequired(),
            addressUIState = addressFormUIState,
            installmentOptions = getInstallmentOptions(
                installmentParams = componentParams.installmentParams,
                cardBrand = selectedOrFirstCardType?.cardBrand,
                isCardTypeReliable = isReliable
            ),
            cardBrands = getCardBrands(filteredDetectedCardTypes),
            isDualBranded = isDualBrandedFlow(filteredDetectedCardTypes),
            kcpBirthDateOrTaxNumberHint = getKcpBirthDateOrTaxNumberHint(inputData.kcpBirthDateOrTaxNumber),
            isCardListVisible = isCardListVisible(getCardBrands(detectedCardTypes), filteredDetectedCardTypes)
        )
    }

    private fun isCardListVisible(
        cardBrands: List<CardListItem>,
        detectedCardTypes: List<DetectedCardType>
    ): Boolean = cardBrands.isNotEmpty() && detectedCardTypes.isEmpty()

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: CardOutputData) {
        Logger.v(TAG, "updateComponentState")
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount", "LongMethod")
    private fun createComponentState(
        outputData: CardOutputData = this.outputData
    ): CardComponentState {
        val cardNumber = outputData.cardNumberState.value

        val firstCardBrand = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
            detectedCardTypes = outputData.detectedCardTypes
        )?.cardBrand

        val binValue =
            if (outputData.cardNumberState.validation.isValid() && cardNumber.length >= EXTENDED_CARD_NUMBER_LENGTH) {
                cardNumber.take(BIN_VALUE_EXTENDED_LENGTH)
            } else {
                cardNumber.take(BIN_VALUE_LENGTH)
            }

        // This safe call is needed because _componentStateFlow is null while this is called the first time.
        @Suppress("UNNECESSARY_SAFE_CALL")
        if (_componentStateFlow?.value?.binValue != binValue) {
            onBinValueListener?.invoke(binValue)
        }

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid || publicKey == null) {
            return CardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = outputData.isValid,
                isReady = publicKey != null,
                cardBrand = firstCardBrand,
                binValue = binValue,
                lastFourDigits = null
            )
        }

        val unencryptedCardBuilder = UnencryptedCard.Builder()

        val encryptedCard: EncryptedCard = try {
            unencryptedCardBuilder.setNumber(outputData.cardNumberState.value)
            if (!isCvcHidden()) {
                val cvc = outputData.securityCodeState.value
                if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
            }
            val expiryDateResult = outputData.expiryDateState.value
            if (expiryDateResult != ExpiryDate.EMPTY_DATE) {
                unencryptedCardBuilder.setExpiryDate(
                    expiryMonth = expiryDateResult.expiryMonth.toString(),
                    expiryYear = expiryDateResult.expiryYear.toString()
                )
            }

            cardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            exceptionChannel.trySend(e)

            return CardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = false,
                isReady = true,
                cardBrand = firstCardBrand,
                binValue = binValue,
                lastFourDigits = null
            )
        }

        return mapComponentState(
            encryptedCard,
            outputData,
            cardNumber,
            firstCardBrand,
            binValue
        )
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state = state)
    }

    // Validation
    private fun validateCardNumber(
        cardNumber: String,
        enableLuhnCheck: Boolean,
        isBrandSupported: Boolean
    ): FieldState<String> {
        val validation = CardValidationUtils.validateCardNumber(cardNumber, enableLuhnCheck, isBrandSupported)
        return cardValidationMapper.mapCardNumberValidation(cardNumber, validation)
    }

    private fun validateExpiryDate(
        expiryDate: ExpiryDate,
        expiryDatePolicy: Brand.FieldPolicy?
    ): FieldState<ExpiryDate> {
        return CardValidationUtils.validateExpiryDate(expiryDate, expiryDatePolicy)
    }

    private fun validateSecurityCode(
        securityCode: String,
        cardType: DetectedCardType?
    ): FieldState<String> {
        return if (componentParams.isHideCvc) {
            FieldState(
                securityCode,
                Validation.Valid
            )
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    private fun validateHolderName(holderName: String): FieldState<String> {
        return if (componentParams.isHolderNameRequired && holderName.isBlank()) {
            FieldState(
                holderName,
                Validation.Invalid(R.string.checkout_holder_name_not_valid)
            )
        } else {
            FieldState(
                holderName,
                Validation.Valid
            )
        }
    }

    private fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String> {
        return if (isSocialSecurityNumberRequired()) {
            SocialSecurityNumberUtils.validateSocialSecurityNumber(socialSecurityNumber)
        } else {
            FieldState(socialSecurityNumber, Validation.Valid)
        }
    }

    private fun validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber: String): FieldState<String> {
        return if (isKCPAuthRequired()) {
            KcpValidationUtils.validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber)
        } else {
            FieldState(kcpBirthDateOrTaxNumber, Validation.Valid)
        }
    }

    private fun validateKcpCardPassword(kcpCardPassword: String): FieldState<String> {
        return if (isKCPAuthRequired()) {
            KcpValidationUtils.validateKcpCardPassword(kcpCardPassword)
        } else {
            FieldState(kcpCardPassword, Validation.Valid)
        }
    }

    private fun validateAddress(
        addressInputModel: AddressInputModel,
        addressFormUIState: AddressFormUIState,
        detectedCardType: DetectedCardType?,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>
    ): AddressOutputData {
        val isOptional =
            CardAddressValidationUtils.isAddressOptional(
                addressParams = componentParams.addressParams,
                cardType = detectedCardType?.cardBrand?.txVariant
            )

        return AddressValidationUtils.validateAddressInput(
            addressInputModel,
            addressFormUIState,
            countryOptions,
            stateOptions,
            isOptional
        )
    }

    private fun isCvcHidden(): Boolean {
        return componentParams.isHideCvc
    }

    private fun isSocialSecurityNumberRequired(): Boolean {
        return componentParams.socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW
    }

    private fun isKCPAuthRequired(): Boolean {
        return componentParams.kcpAuthVisibility == KCPAuthVisibility.SHOW
    }

    private fun getHolderNameUIState(): InputFieldUIState {
        return if (isHolderNameRequired()) InputFieldUIState.REQUIRED else InputFieldUIState.HIDDEN
    }

    private fun isHolderNameRequired(): Boolean {
        return componentParams.isHolderNameRequired
    }

    private fun isAddressRequired(addressFormUIState: AddressFormUIState): Boolean {
        return AddressFormUtils.isAddressRequired(addressFormUIState)
    }

    private fun getFundingSource(): String? {
        return paymentMethod.fundingSource
    }

    private fun getInstallmentOptions(
        installmentParams: InstallmentParams?,
        cardBrand: CardBrand?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel> {
        val isDebit = getFundingSource() == DEBIT_FUNDING_SOURCE
        return if (isDebit) {
            emptyList()
        } else {
            InstallmentUtils.makeInstallmentOptions(installmentParams, cardBrand, isCardTypeReliable)
        }
    }

    private fun requestCountryList() {
        addressRepository.getCountryList(
            shopperLocale = componentParams.shopperLocale,
            coroutineScope = coroutineScope
        )
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(
            shopperLocale = componentParams.shopperLocale,
            countryCode = countryCode,
            coroutineScope = coroutineScope
        )
    }

    private fun makeCvcUIState(cvcPolicy: Brand.FieldPolicy?): InputFieldUIState {
        Logger.d(TAG, "makeCvcUIState: $cvcPolicy")
        return when {
            isCvcHidden() -> InputFieldUIState.HIDDEN
            cvcPolicy?.isRequired() == false -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun makeExpiryDateUIState(expiryDatePolicy: Brand.FieldPolicy?): InputFieldUIState {
        return when {
            expiryDatePolicy?.isRequired() == false -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun makeInstallmentFieldState(installmentModel: InstallmentModel?): FieldState<InstallmentModel?> {
        return FieldState(installmentModel, Validation.Valid)
    }

    private fun mapComponentState(
        encryptedCard: EncryptedCard,
        stateOutputData: CardOutputData,
        cardNumber: String,
        firstCardBrand: CardBrand?,
        binValue: String
    ): CardComponentState {
        val cardPaymentMethod = CardPaymentMethod(
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId(),
        ).apply {
            encryptedCardNumber = encryptedCard.encryptedCardNumber
            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
            encryptedExpiryYear = encryptedCard.encryptedExpiryYear

            if (!isCvcHidden()) {
                encryptedSecurityCode = encryptedCard.encryptedSecurityCode
            }

            if (isHolderNameRequired()) {
                holderName = stateOutputData.holderNameState.value
            }

            if (isKCPAuthRequired()) {
                publicKey?.let { publicKey ->
                    encryptedPassword = genericEncrypter.encryptField(
                        ENCRYPTION_KEY_FOR_KCP_PASSWORD,
                        stateOutputData.kcpCardPasswordState.value,
                        publicKey
                    )
                } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
                taxNumber = stateOutputData.kcpBirthDateOrTaxNumberState.value
            }

            brand = getCardBrand(stateOutputData.detectedCardTypes)

            fundingSource = getFundingSource()

            threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
        }

        val paymentComponentData = makePaymentComponentData(cardPaymentMethod, stateOutputData)

        val lastFour = cardNumber.takeLast(LAST_FOUR_LENGTH)

        return CardComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
            cardBrand = firstCardBrand,
            binValue = binValue,
            lastFourDigits = lastFour
        )
    }

    private fun isDualBrandedFlow(detectedCardTypes: List<DetectedCardType>): Boolean {
        val reliableDetectedCards = detectedCardTypes.filter { it.isReliable }
        return reliableDetectedCards.size > 1
    }

    private fun showStorePaymentField(): Boolean {
        return componentParams.isStorePaymentFieldVisible
    }

    private fun getKcpBirthDateOrTaxNumberHint(input: String): Int {
        return when {
            input.length > KcpValidationUtils.KCP_BIRTH_DATE_LENGTH -> R.string.checkout_kcp_tax_number_hint
            else -> R.string.checkout_kcp_birth_date_or_tax_number_hint
        }
    }

    private fun makePaymentComponentData(
        cardPaymentMethod: CardPaymentMethod,
        stateOutputData: CardOutputData,
    ): PaymentComponentData<CardPaymentMethod> {
        return PaymentComponentData(
            paymentMethod = cardPaymentMethod,
            storePaymentMethod = if (showStorePaymentField()) stateOutputData.shouldStorePaymentMethod else null,
            shopperReference = componentParams.shopperReference,
            order = order,
            amount = componentParams.amount.takeUnless { it.isEmpty },
        ).apply {
            if (isSocialSecurityNumberRequired()) {
                socialSecurityNumber = stateOutputData.socialSecurityNumberState.value
            }
            if (isAddressRequired(stateOutputData.addressUIState)) {
                billingAddress = AddressFormUtils.makeAddressData(
                    addressOutputData = stateOutputData.addressState,
                    addressFormUIState = stateOutputData.addressUIState
                )
            }
            if (isInstallmentsRequired(stateOutputData)) {
                installments = InstallmentUtils.makeInstallmentModelObject(stateOutputData.installmentState.value)
            }
        }
    }

    private fun isInstallmentsRequired(cardOutputData: CardOutputData): Boolean {
        return cardOutputData.installmentOptions.isNotEmpty()
    }

    private fun getCardBrands(detectedCardTypes: List<DetectedCardType>): List<CardListItem> {
        val noCardDetected = detectedCardTypes.isEmpty()
        return componentParams.supportedCardBrands.map { cardBrand ->
            CardListItem(
                cardBrand = cardBrand,
                isDetected = noCardDetected || detectedCardTypes.map { it.cardBrand }.contains(cardBrand),
                environment = componentParams.environment,
            )
        }
    }

    private fun getCardBrand(detectedCardTypes: List<DetectedCardType>): String? {
        return if (isDualBrandedFlow(detectedCardTypes)) {
            DetectedCardTypesUtils.getSelectedCardType(
                detectedCardTypes = detectedCardTypes
            )
        } else {
            val reliableCardBrand = detectedCardTypes.firstOrNull { it.isReliable }
            val firstDetectedBrand = detectedCardTypes.firstOrNull()
            reliableCardBrand ?: firstDetectedBrand
        }?.cardBrand?.txVariant
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setOnBinValueListener(listener: ((binValue: String) -> Unit)?) {
        onBinValueListener = listener
    }

    override fun setOnBinLookupListener(listener: ((data: List<BinLookupData>) -> Unit)?) {
        onBinLookupListener = listener
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
        onBinValueListener = null
        onBinLookupListener = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val DEBIT_FUNDING_SOURCE = "debit"

        @VisibleForTesting
        internal const val BIN_VALUE_LENGTH = 6

        @VisibleForTesting
        internal const val BIN_VALUE_EXTENDED_LENGTH = 8
        private const val EXTENDED_CARD_NUMBER_LENGTH = 16
        private const val LAST_FOUR_LENGTH = 4
        private const val ENCRYPTION_KEY_FOR_KCP_PASSWORD = "password"
    }
}
