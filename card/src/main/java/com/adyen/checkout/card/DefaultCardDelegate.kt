/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.repository.DetectCardTypeRepository
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.AddressFormUtils
import com.adyen.checkout.card.util.AddressValidationUtils
import com.adyen.checkout.card.util.CardValidationUtils
import com.adyen.checkout.card.util.DetectedCardTypesUtils
import com.adyen.checkout.card.util.InstallmentUtils
import com.adyen.checkout.card.util.KcpValidationUtils
import com.adyen.checkout.card.util.SocialSecurityNumberUtils
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
class DefaultCardDelegate(
    private val publicKeyRepository: PublicKeyRepository,
    private val configuration: CardConfiguration,
    private val paymentMethod: PaymentMethod,
    private val addressRepository: AddressRepository,
    private val detectCardTypeRepository: DetectCardTypeRepository,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncrypter: CardEncrypter,
    private val genericEncrypter: GenericEncrypter,
) : CardDelegate {

    override val inputData: CardInputData = CardInputData()

    private var publicKey: String? = null

    private val _outputDataFlow = MutableStateFlow<CardOutputData?>(null)
    override val outputDataFlow: Flow<CardOutputData?> = _outputDataFlow

    private val outputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<CardComponentState?>(null)
    override val componentStateFlow: Flow<CardComponentState?> = _componentStateFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope

        updateOutputData()

        fetchPublicKey()
        subscribeToDetectedCardTypes()

        if (configuration.addressConfiguration is AddressConfiguration.FullAddress) {
            subscribeToStatesList()
            subscribeToCountryList()
            requestCountryList()
        }
    }

    private fun fetchPublicKey() {
        Logger.d(TAG, "fetchPublicKey")
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = configuration.environment,
                clientKey = configuration.clientKey
            ).fold(
                onSuccess = { key ->
                    Logger.d(TAG, "Public key fetched")
                    publicKey = key
                    outputData?.let { createComponentState(it) }
                },
                onFailure = { e ->
                    Logger.e(TAG, "Unable to fetch public key")
                    _exceptionFlow.tryEmit(ComponentException("Unable to fetch publicKey.", e))
                }
            )
        }
    }

    override fun onInputDataChanged(inputData: CardInputData) {
        Logger.v(TAG, "onInputDataChanged")
        detectCardTypeRepository.detectCardType(
            cardNumber = inputData.cardNumber,
            publicKey = publicKey,
            supportedCardTypes = configuration.supportedCardTypes,
            environment = configuration.environment,
            clientKey = configuration.clientKey,
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
                    addressConfiguration = configuration.addressConfiguration,
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

    @Suppress("LongMethod")
    private fun updateOutputData(
        detectedCardTypes: List<DetectedCardType> = outputData?.detectedCardTypes.orEmpty(),
        countryOptions: List<AddressListItem> = outputData?.countryOptions.orEmpty(),
        stateOptions: List<AddressListItem> = outputData?.stateOptions.orEmpty(),
    ) {
        Logger.v(TAG, "updateOutputData")
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
        val selectedOrFirstCardType = getDetectedCardType(filteredDetectedCardTypes)

        // perform a Luhn Check if no brands are detected
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true

        // when no supported cards are detected, only show an error if the brand detection was reliable
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val addressFormUIState = getAddressFormUIState(
            addressConfiguration = configuration.addressConfiguration,
            addressVisibility = configuration.addressVisibility
        )

        val newOutputData = CardOutputData(
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
            addressState = validateAddress(inputData.address, addressFormUIState),
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
                installmentConfiguration = configuration.installmentConfiguration,
                cardType = selectedOrFirstCardType?.cardType,
                isCardTypeReliable = isReliable
            ),
            countryOptions = updatedCountryOptions,
            stateOptions = updatedStateOptions,
            supportedCardTypes = getSupportedCardTypes(),
            isDualBranded = isDualBrandedFlow(filteredDetectedCardTypes),
            kcpBirthDateOrTaxNumberHint = getKcpBirthDateOrTaxNumberHint(inputData.kcpBirthDateOrTaxNumber),
            componentMode = ComponentMode.DEFAULT,
        )

        _outputDataFlow.tryEmit(newOutputData)
        createComponentState(newOutputData)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun createComponentState(outputData: CardOutputData) {
        Logger.v(TAG, "createComponentState")

        val cardNumber = outputData.cardNumberState.value

        val firstCardType = getDetectedCardType(outputData.detectedCardTypes)?.cardType

        val binValue = cardNumber.take(BIN_VALUE_LENGTH)

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid || publicKey == null) {
            _componentStateFlow.tryEmit(
                CardComponentState(
                    paymentComponentData = PaymentComponentData(),
                    isInputValid = outputData.isValid,
                    isReady = publicKey != null,
                    cardType = firstCardType,
                    binValue = binValue,
                    lastFourDigits = null
                )
            )
            return
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
            _exceptionFlow.tryEmit(e)
            _componentStateFlow.tryEmit(
                CardComponentState(
                    paymentComponentData = PaymentComponentData(),
                    isInputValid = false,
                    isReady = true,
                    cardType = firstCardType,
                    binValue = binValue,
                    lastFourDigits = null
                )
            )
            return
        }

        _componentStateFlow.tryEmit(
            mapComponentState(
                encryptedCard,
                outputData,
                cardNumber,
                firstCardType,
                binValue
            )
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
        return if (configuration.isHideCvc) {
            FieldState(
                securityCode,
                Validation.Valid
            )
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    private fun validateHolderName(holderName: String): FieldState<String> {
        return if (configuration.isHolderNameRequired && holderName.isBlank()) {
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
        addressFormUIState: AddressFormUIState
    ): AddressOutputData {
        return AddressValidationUtils.validateAddressInput(addressInputModel, addressFormUIState)
    }

    private fun isCvcHidden(): Boolean {
        return configuration.isHideCvc
    }

    private fun isSocialSecurityNumberRequired(): Boolean {
        return configuration.socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW
    }

    private fun isKCPAuthRequired(): Boolean {
        return configuration.kcpAuthVisibility == KCPAuthVisibility.SHOW
    }

    override fun requiresInput(): Boolean {
        return true
    }

    private fun getHolderNameUIState(): InputFieldUIState {
        return if (isHolderNameRequired()) InputFieldUIState.REQUIRED
        else InputFieldUIState.HIDDEN
    }

    private fun isHolderNameRequired(): Boolean {
        return configuration.isHolderNameRequired
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

    private fun getAddressFormUIState(
        addressConfiguration: AddressConfiguration?,
        addressVisibility: AddressVisibility
    ): AddressFormUIState {
        return AddressFormUtils.getAddressFormUIState(
            addressConfiguration,
            addressVisibility
        )
    }

    private fun getSupportedCardTypes(): List<CardType> = configuration.supportedCardTypes

    private fun requestCountryList() {
        addressRepository.getCountryList(configuration, coroutineScope)
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(configuration, countryCode, coroutineScope)
    }

    private fun makeCvcUIState(cvcPolicy: Brand.FieldPolicy?): InputFieldUIState {
        Logger.d(TAG, "makeCvcUIState: $cvcPolicy")
        return when {
            isCvcHidden() -> InputFieldUIState.HIDDEN
            // We treat CvcPolicy.HIDDEN as OPTIONAL for now to avoid hiding and showing the cvc field while the user
            // is typing the card number.
            cvcPolicy == Brand.FieldPolicy.OPTIONAL
                || cvcPolicy == Brand.FieldPolicy.HIDDEN -> InputFieldUIState.OPTIONAL
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
                brand = getDetectedCardType(stateOutputData.detectedCardTypes)?.cardType?.txVariant
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

    private fun getDetectedCardType(detectedCardTypes: List<DetectedCardType>): DetectedCardType? {
        return DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(detectedCardTypes)
    }

    private fun isDualBrandedFlow(detectedCardTypes: List<DetectedCardType>): Boolean {
        val reliableDetectedCards = detectedCardTypes.filter { it.isReliable }
        return reliableDetectedCards.size > 1 && reliableDetectedCards.any { it.isSelected }
    }

    private fun showStorePaymentField(): Boolean {
        return configuration.isStorePaymentFieldVisible
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
            shopperReference = configuration.shopperReference
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

    override fun onCleared() {
        _coroutineScope = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val DEBIT_FUNDING_SOURCE = "debit"
        private const val BIN_VALUE_LENGTH = 6
        private const val LAST_FOUR_LENGTH = 4
    }
}
