/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card.internal.ui

import android.app.Activity
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.analytics.CardEvents
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardInputData
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.CardAddressValidationUtils
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.card.internal.util.DualBrandedCardHandler
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.card.internal.util.KcpValidationUtils
import com.adyen.checkout.card.internal.util.toBinLookupData
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.internal.util.runCompileOnly
import com.adyen.checkout.core.old.ui.model.ExpiryDate
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.AddressLookupDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.old.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.old.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.old.internal.util.AddressValidationUtils
import com.adyen.checkout.ui.core.old.internal.util.SocialSecurityNumberUtils
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultCardDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val publicKeyRepository: PublicKeyRepository,
    override val componentParams: CardComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    private val analyticsManager: AnalyticsManager,
    private val addressRepository: AddressRepository,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncryptor: BaseCardEncryptor,
    private val genericEncryptor: BaseGenericEncryptor,
    private val submitHandler: SubmitHandler<CardComponentState>,
    private val addressLookupDelegate: AddressLookupDelegate,
    private val cardConfigDataGenerator: CardConfigDataGenerator,
    private val dualBrandedCardHandler: DualBrandedCardHandler,
) : CardDelegate, AddressLookupDelegate by addressLookupDelegate {

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

    override val submitFlow: Flow<CardComponentState> = getTrackedSubmitFlow()
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var onBinValueListener: ((binValue: String) -> Unit)? = null
    private var onBinLookupListener: ((data: List<BinLookupData>) -> Unit)? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope

        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)
        fetchPublicKey()
        subscribeToDetectedCardTypes()
        subscribeToDualBrandedAnalyticsEvents()

        if (componentParams.addressParams is AddressParams.FullAddress ||
            componentParams.addressParams is AddressParams.Lookup
        ) {
            subscribeToStatesList()
            subscribeToCountryList()
            requestCountryList()
        }
        addressLookupDelegate.addressLookupSubmitFlow
            .onEach {
                _viewFlow.tryEmit(CardComponentViewType.DefaultCardView)
                inputData.address.set(it)
                updateOutputData()
            }
            .launchIn(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = paymentMethod.type.orEmpty(),
            configData = cardConfigDataGenerator.generate(configuration = componentParams, isStored = false),
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
        adyenLog(AdyenLogLevel.DEBUG) { "fetchPublicKey" }
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = componentParams.environment,
                clientKey = componentParams.clientKey,
            ).fold(
                onSuccess = { key ->
                    adyenLog(AdyenLogLevel.DEBUG) { "Public key fetched" }
                    publicKey = key
                    updateComponentState(outputData)
                },
                onFailure = { e ->
                    adyenLog(AdyenLogLevel.ERROR) { "Unable to fetch public key" }

                    val event = GenericEvents.error(paymentMethod.type.orEmpty(), ErrorEvent.API_PUBLIC_KEY)
                    analyticsManager.trackEvent(event)

                    exceptionChannel.trySend(ComponentException("Unable to fetch publicKey.", e))
                },
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
        adyenLog(AdyenLogLevel.VERBOSE) { "onInputDataChanged" }
        detectCardTypeRepository.detectCardType(
            cardNumber = inputData.cardNumber,
            publicKey = publicKey,
            supportedCardBrands = componentParams.supportedCardBrands,
            clientKey = componentParams.clientKey,
            coroutineScope = coroutineScope,
            type = paymentMethod.type,
        )
        requestStateList(inputData.address.country)
    }

    private fun subscribeToDetectedCardTypes() {
        detectCardTypeRepository.detectedCardTypesFlow
            .onEach { detectedCardTypes ->
                adyenLog(AdyenLogLevel.DEBUG) {
                    "New detected card types emitted - detectedCardTypes: ${detectedCardTypes.map { it.cardBrand }} " +
                        "- isReliable: ${detectedCardTypes.firstOrNull()?.isReliable}"
                }
                if (detectedCardTypes != outputData.detectedCardTypes) {
                    onBinLookupListener?.invoke(detectedCardTypes.map(DetectedCardType::toBinLookupData))
                }
                updateOutputData(detectedCardTypes = detectedCardTypes)
            }
            .map { detectedCardTypes ->
                detectedCardTypes.filter { it.isReliable && it.isSupported }.map { it.cardBrand }
            }
            .distinctUntilChanged()
            .onEach {
                inputData.selectedCardBrand = null
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToDualBrandedAnalyticsEvents() {
        outputDataFlow.map { it.dualBrandData?.selectedBrand }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { brand ->
                if (inputData.selectedCardBrand != null) {
                    val event = GenericEvents.selected(
                        component = paymentMethod.type.orEmpty(),
                        target = DUAL_BRAND_ANALYTICS_TARGET,
                        brand = brand.txVariant,
                    )
                    analyticsManager.trackEvent(event)
                    adyenLog(AdyenLogLevel.DEBUG) { "brand selection changed: ${brand.txVariant}" }
                }
            }
            .launchIn(coroutineScope)

        outputDataFlow.map { it.dualBrandData?.brandOptions?.map { it.brand.txVariant } }
            .filterNotNull()
            .distinctUntilChanged()
            .map { brandOptions ->
                val event = GenericEvents.displayed(
                    component = paymentMethod.type.orEmpty(),
                    target = DUAL_BRAND_ANALYTICS_TARGET,
                    brand = outputData.dualBrandData?.selectedBrand?.txVariant.orEmpty(),
                    configData = cardConfigDataGenerator.generateDualBrandConfigData(brandOptions),
                )
                analyticsManager.trackEvent(event)
                adyenLog(AdyenLogLevel.DEBUG) { "new brand options: ${brandOptions.joinToString(",")}" }
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToCountryList() {
        addressRepository.countriesFlow
            .distinctUntilChanged()
            .onEach { countries ->
                adyenLog(AdyenLogLevel.DEBUG) { "New countries emitted - countries: ${countries.size}" }
                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    shopperLocale = componentParams.shopperLocale,
                    addressParams = componentParams.addressParams,
                    countryList = countries,
                )
                countryOptions.firstOrNull { it.selected }?.let {
                    inputData.address.country = it.code
                    inputData.address.countryDisplayName = it.name
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
                adyenLog(AdyenLogLevel.DEBUG) { "New states emitted - states: ${states.size}" }
                val stateOptions = AddressFormUtils.initializeStateOptions(states)
                updateOutputData(stateOptions = stateOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun updateOutputData(
        detectedCardTypes: List<DetectedCardType> = outputData.detectedCardTypes,
        countryOptions: List<AddressListItem> = outputData.addressState.countryOptions,
        stateOptions: List<AddressListItem> = outputData.addressState.stateOptions,
    ) {
        val newOutputData =
            createOutputData(detectedCardTypes, countryOptions, stateOptions)
        _outputDataFlow.tryEmit(newOutputData)
        updateComponentState(newOutputData)
    }

    @Suppress("LongMethod")
    private fun createOutputData(
        detectedCardTypes: List<DetectedCardType> = emptyList(),
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
    ): CardOutputData {
        adyenLog(AdyenLogLevel.VERBOSE) { "createOutputData" }
        val updatedCountryOptions = AddressFormUtils.markAddressListItemSelected(
            countryOptions,
            inputData.address.country,
        )
        val updatedStateOptions = AddressFormUtils.markAddressListItemSelected(
            stateOptions,
            inputData.address.stateOrProvince,
        )

        val isReliable = detectedCardTypes.any { it.isReliable }

        val filteredDetectedCardTypes = detectedCardTypes.filter { it.isSupported }

        val selectedOrFirstCardType = inputData.selectedCardBrand?.let { selectedBrand ->
            detectedCardTypes.firstOrNull { it.cardBrand.txVariant == selectedBrand.txVariant }
        } ?: filteredDetectedCardTypes.firstOrNull()

        // perform a Luhn Check if no brands are detected
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true

        // when no supported cards are detected, only show an error if the brand detection was reliable
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val addressFormUIState = AddressFormUIState.fromAddressParams(componentParams.addressParams)

        val addressState = validateAddress(
            inputData.address,
            addressFormUIState,
            selectedOrFirstCardType,
            updatedCountryOptions,
            updatedStateOptions,
        )

        return CardOutputData(
            cardNumberState = validateCardNumber(
                cardNumber = inputData.cardNumber,
                enableLuhnCheck = enableLuhnCheck,
                isBrandSupported = !shouldFailWithUnsupportedBrand,
            ),
            expiryDateState = validateExpiryDate(inputData.expiryDate, selectedOrFirstCardType?.expiryDatePolicy),
            securityCodeState = validateSecurityCode(inputData.securityCode, selectedOrFirstCardType),
            holderNameState = validateHolderName(inputData.holderName),
            socialSecurityNumberState = validateSocialSecurityNumber(inputData.socialSecurityNumber),
            kcpBirthDateOrTaxNumberState = validateKcpBirthDateOrTaxNumber(inputData.kcpBirthDateOrTaxNumber),
            kcpCardPasswordState = validateKcpCardPassword(inputData.kcpCardPassword),
            addressState = addressState,
            installmentState = makeInstallmentFieldState(inputData.installmentOption),
            shouldStorePaymentMethod = inputData.isStorePaymentMethodSwitchChecked,
            cvcUIState = makeCvcUIState(selectedOrFirstCardType),
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
                isCardTypeReliable = isReliable,
            ),
            cardBrands = getCardBrands(filteredDetectedCardTypes),
            kcpBirthDateOrTaxNumberHint = getKcpBirthDateOrTaxNumberHint(inputData.kcpBirthDateOrTaxNumber),
            isCardListVisible = isCardListVisible(getCardBrands(detectedCardTypes), filteredDetectedCardTypes),
            dualBrandData = dualBrandedCardHandler.processDetectedCardTypes(
                detectedCardTypes,
                inputData.selectedCardBrand,
            ),
        )
    }

    private fun isCardListVisible(
        cardBrands: List<CardListItem>,
        detectedCardTypes: List<DetectedCardType>
    ): Boolean = cardBrands.isNotEmpty() &&
        detectedCardTypes.isEmpty() &&
        paymentMethod.type == PaymentMethodTypes.SCHEME

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: CardOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount", "LongMethod")
    private fun createComponentState(
        outputData: CardOutputData = this.outputData
    ): CardComponentState {
        val cardNumber = outputData.cardNumberState.value

        val firstCardBrand = outputData.dualBrandData?.selectedBrand
            ?: outputData.detectedCardTypes.firstOrNull()?.cardBrand

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
                lastFourDigits = null,
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
            if (expiryDateResult != EMPTY_DATE) {
                unencryptedCardBuilder.setExpiryDate(
                    expiryMonth = expiryDateResult.expiryMonth.toString(),
                    expiryYear = expiryDateResult.expiryYear.toString(),
                )
            }

            cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            val event = GenericEvents.error(paymentMethod.type.orEmpty(), ErrorEvent.ENCRYPTION)
            analyticsManager.trackEvent(event)

            exceptionChannel.trySend(e)

            return CardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = false,
                isReady = true,
                cardBrand = firstCardBrand,
                binValue = binValue,
                lastFourDigits = null,
            )
        }

        return mapComponentState(
            encryptedCard,
            outputData,
            cardNumber,
            firstCardBrand,
            binValue,
        )
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state = state)
    }

    override fun startAddressLookup() {
        addressLookupDelegate.initialize(coroutineScope, inputData.address)
        _viewFlow.tryEmit(CardComponentViewType.AddressLookup)
    }

    override fun handleBackPress(): Boolean {
        return if (_viewFlow.value == CardComponentViewType.AddressLookup) {
            addressDelegate.updateAddressInputData { reset() }
            _viewFlow.tryEmit(CardComponentViewType.DefaultCardView)
            true
        } else {
            false
        }
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
        val validation = CardValidationUtils.validateExpiryDate(expiryDate, expiryDatePolicy)
        return cardValidationMapper.mapExpiryDateValidation(expiryDate, validation)
    }

    private fun validateSecurityCode(
        securityCode: String,
        cardType: DetectedCardType?
    ): FieldState<String> {
        val cvcUIState = makeCvcUIState(cardType)
        val validation = CardValidationUtils.validateSecurityCode(securityCode, cardType, cvcUIState)
        return cardValidationMapper.mapSecurityCodeValidation(securityCode, validation)
    }

    private fun validateHolderName(holderName: String): FieldState<String> {
        return if (componentParams.isHolderNameRequired && holderName.isBlank()) {
            FieldState(
                holderName,
                Validation.Invalid(R.string.checkout_holder_name_not_valid),
            )
        } else {
            FieldState(
                holderName,
                Validation.Valid,
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
                cardType = detectedCardType?.cardBrand?.txVariant,
            )

        return AddressValidationUtils.validateAddressInput(
            addressInputModel,
            addressFormUIState,
            countryOptions,
            stateOptions,
            isOptional,
        )
    }

    private fun isCvcHidden(cvcUIState: InputFieldUIState = outputData.cvcUIState): Boolean {
        return cvcUIState == InputFieldUIState.HIDDEN
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
            coroutineScope = coroutineScope,
        )
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(
            shopperLocale = componentParams.shopperLocale,
            countryCode = countryCode,
            coroutineScope = coroutineScope,
        )
    }

    private fun makeCvcUIState(detectedCardType: DetectedCardType?): InputFieldUIState {
        adyenLog(AdyenLogLevel.DEBUG) { "makeCvcUIState: ${detectedCardType?.cvcPolicy}" }

        return if (detectedCardType?.isReliable == true) {
            when (componentParams.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW -> {
                    when (detectedCardType.cvcPolicy) {
                        Brand.FieldPolicy.OPTIONAL -> InputFieldUIState.OPTIONAL
                        Brand.FieldPolicy.HIDDEN -> InputFieldUIState.HIDDEN
                        else -> InputFieldUIState.REQUIRED
                    }
                }

                CVCVisibility.HIDE_FIRST -> {
                    when (detectedCardType.cvcPolicy) {
                        Brand.FieldPolicy.REQUIRED -> InputFieldUIState.REQUIRED
                        Brand.FieldPolicy.OPTIONAL -> InputFieldUIState.OPTIONAL
                        else -> InputFieldUIState.HIDDEN
                    }
                }

                CVCVisibility.ALWAYS_HIDE -> InputFieldUIState.HIDDEN
            }
        } else {
            when (componentParams.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW -> InputFieldUIState.REQUIRED
                CVCVisibility.HIDE_FIRST -> InputFieldUIState.HIDDEN
                CVCVisibility.ALWAYS_HIDE -> InputFieldUIState.HIDDEN
            }
        }
    }

    private fun makeExpiryDateUIState(expiryDatePolicy: Brand.FieldPolicy?): InputFieldUIState {
        return when (expiryDatePolicy) {
            Brand.FieldPolicy.OPTIONAL -> InputFieldUIState.OPTIONAL
            Brand.FieldPolicy.HIDDEN -> InputFieldUIState.HIDDEN
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
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
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
                    encryptedPassword = genericEncryptor.encryptField(
                        ENCRYPTION_KEY_FOR_KCP_PASSWORD,
                        stateOutputData.kcpCardPasswordState.value,
                        publicKey,
                    )
                } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
                taxNumber = stateOutputData.kcpBirthDateOrTaxNumberState.value
            }

            brand = getCardBrand(stateOutputData.detectedCardTypes, stateOutputData.dualBrandData)

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
            lastFourDigits = lastFour,
        )
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
            amount = componentParams.amount,
        ).apply {
            if (isSocialSecurityNumberRequired()) {
                socialSecurityNumber = stateOutputData.socialSecurityNumberState.value
            }
            if (isAddressRequired(stateOutputData.addressUIState)) {
                billingAddress = AddressFormUtils.makeAddressData(
                    addressOutputData = stateOutputData.addressState,
                    addressFormUIState = stateOutputData.addressUIState,
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

    private fun getCardBrand(
        detectedCardTypes: List<DetectedCardType>,
        dualBrandData: DualBrandData?
    ): String? {
        return if (dualBrandData != null) {
            dualBrandData.selectedBrand?.txVariant
        } else {
            val reliableCardBrand = detectedCardTypes.firstOrNull { it.isReliable }
            val firstDetectedBrand = detectedCardTypes.firstOrNull()
            val cardType = reliableCardBrand ?: firstDetectedBrand
            cardType?.cardBrand?.txVariant
        }
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setOnBinValueListener(listener: ((binValue: String) -> Unit)?) {
        onBinValueListener = listener
    }

    override fun setOnBinLookupListener(listener: ((data: List<BinLookupData>) -> Unit)?) {
        onBinLookupListener = listener
    }

    override fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback) {
        addressLookupDelegate.setAddressLookupCallback(addressLookupCallback)
    }

    override fun updateAddressLookupOptions(options: List<LookupAddress>) {
        adyenLog(AdyenLogLevel.DEBUG) { "update address lookup options $options" }
        addressLookupDelegate.updateAddressLookupOptions(options)
    }

    override fun setAddressLookupResult(addressLookupResult: AddressLookupResult) {
        addressLookupDelegate.setAddressLookupResult(addressLookupResult)
    }

    override fun onCardScanningAvailability(isAvailable: Boolean) {
        val event = if (isAvailable) {
            CardEvents.cardScannerAvailable(getPaymentMethodType())
        } else {
            CardEvents.cardScannerUnavailable(getPaymentMethodType())
        }
        analyticsManager.trackEvent(event)
    }

    override fun onCardScanningDisplayed(didDisplay: Boolean) {
        val event = if (didDisplay) {
            CardEvents.cardScannerPresented(getPaymentMethodType())
        } else {
            CardEvents.cardScannerFailure(getPaymentMethodType())
        }
        analyticsManager.trackEvent(event)
    }

    override fun onCardScanningResult(resultCode: Int, pan: String?, expiryMonth: Int?, expiryYear: Int?) {
        val event = when {
            resultCode == Activity.RESULT_CANCELED -> CardEvents.cardScannerCancelled(getPaymentMethodType())
            pan == null && expiryMonth == null && expiryYear == null ->
                CardEvents.cardScannerFailure(getPaymentMethodType())

            resultCode == Activity.RESULT_OK -> CardEvents.cardScannerSuccess(getPaymentMethodType())
            else -> null
        }
        event?.let { analyticsManager.trackEvent(event) }

        updateInputData {
            pan?.let { cardNumber = pan }
            if (expiryMonth != null && expiryYear != null) {
                expiryDate = ExpiryDate(expiryMonth, expiryYear)
            }
        }
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
        onBinValueListener = null
        onBinLookupListener = null
        addressLookupDelegate.clear()
        analyticsManager.clear(this)
    }

    companion object {
        private const val DEBIT_FUNDING_SOURCE = "debit"
        private const val DUAL_BRAND_ANALYTICS_TARGET = "dual_brand_button"

        @VisibleForTesting
        internal const val BIN_VALUE_LENGTH = 6

        @VisibleForTesting
        internal const val BIN_VALUE_EXTENDED_LENGTH = 8
        private const val EXTENDED_CARD_NUMBER_LENGTH = 16
        private const val LAST_FOUR_LENGTH = 4
        private const val ENCRYPTION_KEY_FOR_KCP_PASSWORD = "password"
    }
}
