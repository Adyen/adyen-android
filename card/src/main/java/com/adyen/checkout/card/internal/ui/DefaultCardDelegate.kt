/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card.internal.ui

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardDelegateState
import com.adyen.checkout.card.internal.ui.model.CardFieldId
import com.adyen.checkout.card.internal.ui.model.CardInputData
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.CardViewState
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.toViewState
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.CardAddressValidationUtils
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.card.internal.util.KcpValidationUtils
import com.adyen.checkout.card.internal.util.toBinLookupData
import com.adyen.checkout.card.toComponentState
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
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateStateManager
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
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
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.checkout.ui.core.internal.util.SocialSecurityNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    // TODO: Should not be nullable
    private val stateManager: DelegateStateManager<CardDelegateState, CardFieldId>? = null,
) : CardDelegate, AddressLookupDelegate by addressLookupDelegate {

    override val componentStateFlow: StateFlow<CardComponentState> by lazy {
        val toComponentState: (CardDelegateState) -> CardComponentState = { delegateState ->
            delegateState.toComponentState(
                paymentMethod = paymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                cardEncryptor = cardEncryptor,
                genericEncryptor = genericEncryptor,
                exceptionChannel = exceptionChannel,
                onBinValueListener = onBinValueListener,
            )
        }
        stateManager!!.state
            .map(toComponentState)
            .stateIn(coroutineScope, SharingStarted.Lazily, toComponentState(stateManager.state.value))
    }

    override val viewStateFlow: Flow<CardViewState> by lazy {
        stateManager!!.state
            .map(CardDelegateState::toViewState)
            .stateIn(coroutineScope, SharingStarted.Lazily, stateManager.state.value.toViewState())
    }

    private val inputData: CardInputData = CardInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<CardOutputData> = _outputDataFlow

    override val addressOutputData: AddressOutputData
        get() = stateManager!!.state.value.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        stateManager!!.state.map {
            it.addressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, stateManager.state.value.addressState)
    }

    override val outputData: CardOutputData
        get() = _outputDataFlow.value

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
                stateManager?.updateState {
                    copy(address = it)
                }
                validateAndUpdateAddress()
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
                    stateManager?.updateState {
                        copy(publicKey = key)
                    }
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
//        onInputDataChanged()
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        val currentAddress = stateManager!!.state.value.address.copy()
        currentAddress.update()

        stateManager.updateState {
            copy(address = currentAddress)
        }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    private fun detectCardType() {
        detectCardTypeRepository.detectCardType(
            cardNumber = stateManager!!.state.value.cardNumberDelegateState.value,
            publicKey = stateManager.state.value.publicKey,
            supportedCardBrands = componentParams.supportedCardBrands,
            clientKey = componentParams.clientKey,
            coroutineScope = coroutineScope,
            type = paymentMethod.type,
        )
        requestStateList(stateManager.state.value.address.country)
    }

    private fun subscribeToDetectedCardTypes() {
        detectCardTypeRepository.detectedCardTypesFlow
            .onEach { detectedCardTypes ->
                adyenLog(AdyenLogLevel.DEBUG) {
                    "New detected card types emitted - detectedCardTypes: ${detectedCardTypes.map { it.cardBrand }} " +
                        "- isReliable: ${detectedCardTypes.firstOrNull()?.isReliable}"
                }
                if (detectedCardTypes != stateManager!!.state.value.detectedCardTypes) {
                    onBinLookupListener?.invoke(detectedCardTypes.map(DetectedCardType::toBinLookupData))
                }
                detectedCardTypesUpdated(detectedCardTypes)
            }
            .map { detectedCardTypes -> detectedCardTypes.map { it.cardBrand } }
            .distinctUntilChanged()
            // TODO: Why do we have this?
            .onEach { inputData.selectedCardIndex = -1 }
            .launchIn(coroutineScope)
    }

    private fun detectedCardTypesUpdated(detectedCardTypes: List<DetectedCardType>) {
        val isReliable = detectedCardTypes.any { it.isReliable }

        val filteredDetectedCardTypes = DetectedCardTypesUtils.filterDetectedCardTypes(
            detectedCardTypes,
            stateManager!!.state.value.selectedCardIndexDelegateState.value,
        )
        val selectedOrFirstCardType = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
            detectedCardTypes = filteredDetectedCardTypes,
        )

        // when no supported cards are detected, only show an error if the brand detection was reliable
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val cardBrands = getCardBrands(filteredDetectedCardTypes)
        val isCardBrandListVisible = isCardListVisible(getCardBrands(detectedCardTypes), filteredDetectedCardTypes)
        val isDualBranded = isDualBrandedFlow(filteredDetectedCardTypes)

        val installmentOptions = getInstallmentOptions(
            installmentParams = componentParams.installmentParams,
            cardBrand = selectedOrFirstCardType?.cardBrand,
            isCardTypeReliable = isReliable,
        )
        val installmentOptionValue =
            stateManager.state.value.installmentOptionDelegateState.value ?: installmentOptions.firstOrNull()

        stateManager.updateState {
            copy(
                detectedCardTypes = filteredDetectedCardTypes,
                selectedOrFirstCardType = selectedOrFirstCardType,
                cardBrands = cardBrands,
                isCardBrandListVisible = isCardBrandListVisible,
                isDualBranded = isDualBranded,
                cvcUIState = makeCvcUIState(selectedOrFirstCardType),
                expiryDateUIState = makeExpiryDateUIState(selectedOrFirstCardType?.expiryDatePolicy),
                enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true,
                isBrandSupported = !shouldFailWithUnsupportedBrand,
                installmentOptions = installmentOptions,
                installmentOptionDelegateState = installmentOptionDelegateState.copy(value = installmentOptionValue),
                isAddressOptional = CardAddressValidationUtils.isAddressOptional(
                    addressParams = componentParams.addressParams,
                    cardType = selectedOrFirstCardType?.cardBrand?.txVariant,
                ),
            )
        }
        validateAndUpdateAddress()

        // TODO Create a new function to re-validate the field?
        stateManager.updateField(CardFieldId.CARD_NUMBER, stateManager.state.value.cardNumberDelegateState.value)
        stateManager.updateField(
            CardFieldId.CARD_SECURITY_CODE,
            stateManager.state.value.cardSecurityCodeDelegateState.value,
        )
        stateManager.updateField(
            CardFieldId.CARD_EXPIRY_DATE,
            stateManager.state.value.cardExpiryDateDelegateState.value,
        )
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
                    stateManager?.updateState {
                        copy(address = address.copy(country = it.code))
                    }
                    requestStateList(it.code)
                }

                countryOptionsUpdated(countryOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun countryOptionsUpdated(countryOptions: List<AddressListItem>) {
        val updatedCountryOptions = AddressFormUtils.markAddressListItemSelected(
            countryOptions,
            stateManager!!.state.value.address.country,
        )

        stateManager.updateState {
            copy(updatedCountryOptions = updatedCountryOptions)
        }
        validateAndUpdateAddress()
    }

    private fun subscribeToStatesList() {
        addressRepository.statesFlow
            .distinctUntilChanged()
            .onEach { states ->
                adyenLog(AdyenLogLevel.DEBUG) { "New states emitted - states: ${states.size}" }
                val stateOptions = AddressFormUtils.initializeStateOptions(states)

                stateOptionsUpdated(stateOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun stateOptionsUpdated(stateOptions: List<AddressListItem>) {
        val updatedStateOptions = AddressFormUtils.markAddressListItemSelected(
            stateOptions,
            stateManager!!.state.value.address.stateOrProvince,
        )

        stateManager.updateState {
            copy(updatedStateOptions = updatedStateOptions)
        }
        validateAndUpdateAddress()
    }

    private fun validateAndUpdateAddress() = with(stateManager!!.state.value) {
        val addressState = validateAddress(
            address,
            addressFormUIState,
            selectedOrFirstCardType,
            updatedCountryOptions,
            updatedStateOptions,
        )

        stateManager.updateState {
            copy(addressState = addressState)
        }
        stateManager.updateField(
            CardFieldId.ADDRESS_LOOKUP,
            addressState.formatted(),
        )
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

        val filteredDetectedCardTypes = DetectedCardTypesUtils.filterDetectedCardTypes(
            detectedCardTypes,
            inputData.selectedCardIndex,
        )
        val selectedOrFirstCardType = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
            detectedCardTypes = filteredDetectedCardTypes,
        )

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
//            installmentState = makeInstallmentFieldState(inputData.installmentOption),
//            shouldStorePaymentMethod = inputData.isStorePaymentMethodSwitchChecked,
//            cvcUIState = makeCvcUIState(selectedOrFirstCardType),
//            expiryDateUIState = makeExpiryDateUIState(selectedOrFirstCardType?.expiryDatePolicy),
//            holderNameUIState = getHolderNameUIState(),
//            showStorePaymentField = showStorePaymentField(),
            detectedCardTypes = filteredDetectedCardTypes,
//            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired(),
//            isKCPAuthRequired = isKCPAuthRequired(),
            addressUIState = addressFormUIState,
//            installmentOptions = getInstallmentOptions(
//                installmentParams = componentParams.installmentParams,
//                cardBrand = selectedOrFirstCardType?.cardBrand,
//                isCardTypeReliable = isReliable,
//            ),
            cardBrands = getCardBrands(filteredDetectedCardTypes),
            isDualBranded = isDualBrandedFlow(filteredDetectedCardTypes),
//            kcpBirthDateOrTaxNumberHint = getKcpBirthDateOrTaxNumberHint(inputData.kcpBirthDateOrTaxNumber),
            isCardListVisible = isCardListVisible(getCardBrands(detectedCardTypes), filteredDetectedCardTypes),
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

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun <T> onFieldValueChanged(fieldId: CardFieldId, value: T) {
        stateManager!!.updateField(fieldId, value = value)
        detectCardType()
    }

    override fun onFieldFocusChanged(fieldId: CardFieldId, hasFocus: Boolean) {
        stateManager!!.updateField<Unit>(fieldId, hasFocus = hasFocus)
    }

    override fun onSubmit() = if (stateManager!!.isValid) {
        submitHandler.onSubmit(componentStateFlow.value)
    } else {
        stateManager.highlightAllFieldValidationErrors()
    }

    override fun startAddressLookup() {
        addressLookupDelegate.initialize(coroutineScope, stateManager!!.state.value.address)
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

    private fun isCvcHidden(cvcUIState: InputFieldUIState = stateManager!!.state.value.cvcUIState): Boolean {
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
            storePaymentMethod = if (showStorePaymentField()) stateManager!!.state.value.storedPaymentMethodSwitchDelegateState.value else null,
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
                installments =
                    InstallmentUtils.makeInstallmentModelObject(stateManager!!.state.value.installmentOptionDelegateState.value)
            }
        }
    }

    private fun isInstallmentsRequired(cardOutputData: CardOutputData): Boolean {
        return stateManager!!.state.value.installmentOptions.isNotEmpty()
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
                detectedCardTypes = detectedCardTypes,
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
    }
}
