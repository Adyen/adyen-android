/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.repository.DetectCardTypeRepository
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.ui.model.CardListItem
import com.adyen.checkout.card.util.AddressFormUtils
import com.adyen.checkout.card.util.AddressValidationUtils
import com.adyen.checkout.card.util.CardAddressValidationUtils
import com.adyen.checkout.card.util.CardValidationUtils
import com.adyen.checkout.card.util.DetectedCardTypesUtils
import com.adyen.checkout.card.util.InstallmentUtils
import com.adyen.checkout.card.util.KcpValidationUtils
import com.adyen.checkout.card.util.SocialSecurityNumberUtils
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.PaymentComponentUIEvent
import com.adyen.checkout.components.ui.PaymentComponentUIState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.ButtonComponentViewType
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.GenericEncrypter
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
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
    private val analyticsRepository: AnalyticsRepository,
    private val addressRepository: AddressRepository,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncrypter: CardEncrypter,
    private val genericEncrypter: GenericEncrypter,
    private val submitHandler: SubmitHandler
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

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(CardComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val submitChannel: Channel<CardComponentState> = bufferedChannel()
    override val submitFlow: Flow<CardComponentState> = submitChannel.receiveAsFlow()

    private val _uiStateFlow = MutableStateFlow<PaymentComponentUIState>(PaymentComponentUIState.Idle)
    override val uiStateFlow: Flow<PaymentComponentUIState> = _uiStateFlow

    private val _uiEventChannel: Channel<PaymentComponentUIEvent> = bufferedChannel()
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = _uiEventChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)

        sendAnalyticsEvent(coroutineScope)
        fetchPublicKey()
        subscribeToDetectedCardTypes()

        if (componentParams.addressParams is AddressParams.FullAddress) {
            subscribeToStatesList()
            subscribeToCountryList()
            requestCountryList()
        }
    }

    private fun sendAnalyticsEvent(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "sendAnalyticsEvent")
        coroutineScope.launch {
            analyticsRepository.sendAnalyticsEvent()
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

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        detectCardTypeRepository.detectCardType(
            cardNumber = inputData.cardNumber,
            publicKey = publicKey,
            supportedCardTypes = componentParams.supportedCardTypes,
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
                    "New detected card types emitted - detectedCardTypes: ${detectedCardTypes.map { it.cardType }} " +
                        "- isReliable: ${detectedCardTypes.firstOrNull()?.isReliable}"
                )
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
            isStoredPaymentMethodEnable = inputData.isStorePaymentSelected,
            cvcUIState = makeCvcUIState(selectedOrFirstCardType?.cvcPolicy),
            expiryDateUIState = makeExpiryDateUIState(selectedOrFirstCardType?.expiryDatePolicy),
            holderNameUIState = getHolderNameUIState(),
            showStorePaymentField = showStorePaymentField(),
            detectedCardTypes = filteredDetectedCardTypes,
            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired(),
            isKCPAuthRequired = isKCPAuthRequired(),
            addressUIState = addressFormUIState,
            installmentOptions = getInstallmentOptions(
                installmentConfiguration = componentParams.installmentConfiguration,
                cardType = selectedOrFirstCardType?.cardType,
                isCardTypeReliable = isReliable
            ),
            cardBrands = getCardBrands(filteredDetectedCardTypes),
            isDualBranded = isDualBrandedFlow(filteredDetectedCardTypes),
            kcpBirthDateOrTaxNumberHint = getKcpBirthDateOrTaxNumberHint(inputData.kcpBirthDateOrTaxNumber),
            componentMode = ComponentMode.DEFAULT,
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

    private fun onState(state: CardComponentState) {
        val uiState = _uiStateFlow.value
        if (uiState == PaymentComponentUIState.Loading) {
            if (state.isValid) {
                submitChannel.trySend(state)
            } else {
                _uiStateFlow.tryEmit(PaymentComponentUIState.Idle)
            }
        }
    }

    @Suppress("ReturnCount")
    private fun createComponentState(
        outputData: CardOutputData = this.outputData
    ): CardComponentState {
        val cardNumber = outputData.cardNumberState.value

        val firstCardType = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
            detectedCardTypes = outputData.detectedCardTypes
        )?.cardType

        val binValue = cardNumber.take(BIN_VALUE_LENGTH)

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid || publicKey == null) {
            return CardComponentState(
                paymentComponentData = PaymentComponentData(),
                isInputValid = outputData.isValid,
                isReady = publicKey != null,
                cardType = firstCardType,
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
                unencryptedCardBuilder.setExpiryMonth(expiryDateResult.expiryMonth.toString())
                unencryptedCardBuilder.setExpiryYear(expiryDateResult.expiryYear.toString())
            }

            cardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            exceptionChannel.trySend(e)

            return CardComponentState(
                paymentComponentData = PaymentComponentData(),
                isInputValid = false,
                isReady = true,
                cardType = firstCardType,
                binValue = binValue,
                lastFourDigits = null
            )
        }

        return mapComponentState(
            encryptedCard,
            outputData,
            cardNumber,
            firstCardType,
            binValue
        )
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(
            state = state,
            submitChannel = submitChannel,
            uiEventChannel = _uiEventChannel,
            uiStateChannel = _uiStateFlow
        )
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
                cardType = detectedCardType?.cardType?.txVariant
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
        installmentConfiguration: InstallmentConfiguration?,
        cardType: CardType?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel> {
        val isDebit = getFundingSource() == DEBIT_FUNDING_SOURCE
        return if (isDebit) {
            emptyList()
        } else {
            InstallmentUtils.makeInstallmentOptions(installmentConfiguration, cardType, isCardTypeReliable)
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
            // We treat CvcPolicy.HIDDEN as OPTIONAL for now to avoid hiding and showing the cvc field while the user
            // is typing the card number.
            cvcPolicy == Brand.FieldPolicy.OPTIONAL ||
                cvcPolicy == Brand.FieldPolicy.HIDDEN -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun makeExpiryDateUIState(expiryDatePolicy: Brand.FieldPolicy?): InputFieldUIState {
        return when (expiryDatePolicy) {
            Brand.FieldPolicy.OPTIONAL, Brand.FieldPolicy.HIDDEN -> InputFieldUIState.OPTIONAL
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
        firstCardType: CardType?,
        binValue: String
    ): CardComponentState {
        val cardPaymentMethod = CardPaymentMethod().apply {
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE

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
                        GenericEncrypter.KCP_PASSWORD_KEY,
                        stateOutputData.kcpCardPasswordState.value,
                        publicKey
                    )
                } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
                taxNumber = stateOutputData.kcpBirthDateOrTaxNumberState.value
            }

            if (isDualBrandedFlow(stateOutputData.detectedCardTypes)) {
                brand = DetectedCardTypesUtils.getSelectedCardType(
                    detectedCardTypes = stateOutputData.detectedCardTypes
                )?.cardType?.txVariant
            }

            fundingSource = getFundingSource()

            try {
                threeDS2SdkVersion = ThreeDS2Service.INSTANCE.sdkVersion
            } catch (e: ClassNotFoundException) {
                Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
            } catch (e: NoClassDefFoundError) {
                Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
            }
        }

        val paymentComponentData = makePaymentComponentData(cardPaymentMethod, stateOutputData)

        val lastFour = cardNumber.takeLast(LAST_FOUR_LENGTH)

        return CardComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = true,
            isReady = true,
            cardType = firstCardType,
            binValue = binValue,
            lastFourDigits = lastFour
        )
    }

    private fun isDualBrandedFlow(detectedCardTypes: List<DetectedCardType>): Boolean {
        val reliableDetectedCards = detectedCardTypes.filter { it.isReliable }
        return reliableDetectedCards.size > 1 && reliableDetectedCards.any { it.isSelected }
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
        stateOutputData: CardOutputData
    ): PaymentComponentData<CardPaymentMethod> {
        return PaymentComponentData<CardPaymentMethod>().apply {
            paymentMethod = cardPaymentMethod
            storePaymentMethod = stateOutputData.isStoredPaymentMethodEnable
            shopperReference = componentParams.shopperReference
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
        return componentParams.supportedCardTypes.map { cardType ->
            CardListItem(
                cardType = cardType,
                isDetected = noCardDetected || detectedCardTypes.map { it.cardType }.contains(cardType),
                environment = componentParams.environment,
            )
        }
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val DEBIT_FUNDING_SOURCE = "debit"
        private const val BIN_VALUE_LENGTH = 6
        private const val LAST_FOUR_LENGTH = 4
    }
}
