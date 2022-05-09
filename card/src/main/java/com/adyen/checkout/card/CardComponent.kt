/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.AddressFormUtils
import com.adyen.checkout.card.util.DualBrandedCardUtils
import com.adyen.checkout.card.util.InstallmentUtils
import com.adyen.checkout.card.util.KcpValidationUtils
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

private const val BIN_VALUE_LENGTH = 6
private const val LAST_FOUR_LENGTH = 4
private const val SINGLE_CARD_LIST_SIZE = 1

@Suppress("TooManyFunctions")
class CardComponent private constructor(
    savedStateHandle: SavedStateHandle,
    private val cardDelegate: CardDelegate,
    private val cardConfiguration: CardConfiguration
) : BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardComponentState>(
    savedStateHandle,
    cardDelegate,
    cardConfiguration
) {

    internal val inputData = CardInputData()

    private var publicKey: String? = null

    init {
        viewModelScope.launch {
            try {
                publicKey = cardDelegate.fetchPublicKey()
                notifyStateChanged()
            } catch (e: CheckoutException) {
                notifyException(ComponentException("Unable to fetch publicKey.", e))
            }
        }

        if (cardDelegate is NewCardDelegate) {
            cardDelegate.binLookupFlow
                .onEach {
                    Logger.d(TAG, "New binLookupFlow emitted")
                    Logger.d(TAG, "Brands: $it")
                    with(outputData) {
                        this ?: return@with
                        val newOutputData = makeOutputData(
                            cardNumber = cardNumberState.value,
                            expiryDate = expiryDateState.value,
                            securityCode = securityCodeState.value,
                            holderName = holderNameState.value,
                            socialSecurityNumber = socialSecurityNumberState.value,
                            kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumberState.value,
                            kcpCardPassword = kcpCardPasswordState.value,
                            addressInputModel = inputData.address,
                            isStorePaymentSelected = isStoredPaymentMethodEnable,
                            detectedCardTypes = it,
                            selectedCardIndex = inputData.selectedCardIndex,
                            selectedInstallmentOption = inputData.installmentOption,
                            countryOptions = countryOptions,
                            stateOptions = stateOptions
                        )
                        notifyStateChanged(newOutputData)
                    }
                }
                .launchIn(viewModelScope)

            if (configuration.addressConfiguration is AddressConfiguration.FullAddress) {
                subscribeToStatesList(cardDelegate)
                requestCountryList(cardDelegate)
            }
        }
    }

    constructor(
        savedStateHandle: SavedStateHandle,
        storedCardDelegate: StoredCardDelegate,
        cardConfiguration: CardConfiguration
    ) : this(
        savedStateHandle,
        storedCardDelegate as CardDelegate,
        cardConfiguration
    ) {
        storedCardDelegate.updateInputData(inputData)

        // TODO: 09/12/2020 move this logic to base component, maybe create the inputdata from the delegate?
        if (!requiresInput()) {
            inputDataChanged(inputData)
        }
    }

    constructor(
        savedStateHandle: SavedStateHandle,
        cardDelegate: NewCardDelegate,
        cardConfiguration: CardConfiguration
    ) : this(
        savedStateHandle,
        cardDelegate as CardDelegate,
        cardConfiguration
    )

    override fun requiresInput(): Boolean {
        return cardDelegate.requiresInput()
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> {
        return PAYMENT_METHOD_TYPES
    }

    override fun onInputDataChanged(inputData: CardInputData): CardOutputData {
        Logger.v(TAG, "onInputDataChanged")

        val detectedCardTypes = cardDelegate.detectCardType(inputData.cardNumber, publicKey, viewModelScope)
        if (cardDelegate is NewCardDelegate) {
            cardDelegate.requestStateList(inputData.address.country, viewModelScope)
        }

        return makeOutputData(
            cardNumber = inputData.cardNumber,
            expiryDate = inputData.expiryDate,
            securityCode = inputData.securityCode,
            holderName = inputData.holderName,
            socialSecurityNumber = inputData.socialSecurityNumber,
            kcpBirthDateOrTaxNumber = inputData.kcpBirthDateOrTaxNumber,
            kcpCardPassword = inputData.kcpCardPassword,
            addressInputModel = inputData.address,
            isStorePaymentSelected = inputData.isStorePaymentSelected,
            detectedCardTypes = detectedCardTypes,
            selectedCardIndex = inputData.selectedCardIndex,
            selectedInstallmentOption = inputData.installmentOption,
            countryOptions = AddressFormUtils.markAddressListItemSelected(outputData?.countryOptions.orEmpty(), inputData.address.country),
            stateOptions = AddressFormUtils.markAddressListItemSelected(outputData?.stateOptions.orEmpty(), inputData.address.stateOrProvince)
        )
    }

    @Suppress("LongParameterList")
    private fun makeOutputData(
        cardNumber: String,
        expiryDate: ExpiryDate,
        securityCode: String,
        holderName: String,
        socialSecurityNumber: String,
        kcpBirthDateOrTaxNumber: String,
        kcpCardPassword: String,
        addressInputModel: AddressInputModel,
        isStorePaymentSelected: Boolean,
        detectedCardTypes: List<DetectedCardType>,
        selectedCardIndex: Int,
        selectedInstallmentOption: InstallmentModel?,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>
    ): CardOutputData {

        val isReliable = detectedCardTypes.any { it.isReliable }
        val supportedCardTypes = detectedCardTypes.filter { it.isSupported }
        val sortedCardTypes = DualBrandedCardUtils.sortBrands(supportedCardTypes)
        val outputCardTypes = markSelectedCard(sortedCardTypes, selectedCardIndex)

        val selectedOrFirstCardType = outputCardTypes.firstOrNull { it.isSelected } ?: outputCardTypes.firstOrNull()

        // perform a Luhn Check if no brands are detected
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true

        // when no supported cards are detected, only show an error if the brand detection was reliable
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val addressFormUIState = cardDelegate.getAddressFormUIState(
            configuration.addressConfiguration,
            configuration.addressVisibility
        )

        return CardOutputData(
            cardDelegate.validateCardNumber(cardNumber, enableLuhnCheck, isBrandSupported = !shouldFailWithUnsupportedBrand),
            cardDelegate.validateExpiryDate(expiryDate, selectedOrFirstCardType?.expiryDatePolicy),
            cardDelegate.validateSecurityCode(securityCode, selectedOrFirstCardType),
            cardDelegate.validateHolderName(holderName),
            cardDelegate.validateSocialSecurityNumber(socialSecurityNumber),
            cardDelegate.validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber),
            cardDelegate.validateKcpCardPassword(kcpCardPassword),
            cardDelegate.validateAddress(addressInputModel, addressFormUIState),
            makeInstallmentFieldState(selectedInstallmentOption),
            isStorePaymentSelected,
            makeCvcUIState(selectedOrFirstCardType?.cvcPolicy),
            makeExpiryDateUIState(selectedOrFirstCardType?.expiryDatePolicy),
            outputCardTypes,
            cardDelegate.isSocialSecurityNumberRequired(),
            cardDelegate.isKCPAuthRequired(),
            addressFormUIState,
            cardDelegate.getInstallmentOptions(
                configuration.installmentConfiguration,
                selectedOrFirstCardType?.cardType,
                isReliable
            ),
            countryOptions,
            stateOptions
        )
    }

    private fun subscribeToStatesList(cardDelegate: NewCardDelegate) {
        cardDelegate.stateListFlow
            .distinctUntilChanged()
            .onEach {
                Logger.d(TAG, "New states emitted")
                Logger.d(TAG, "States: $it")
                with(outputData) {
                    this ?: return@with
                    val newOutputData = makeOutputData(
                        cardNumber = cardNumberState.value,
                        expiryDate = expiryDateState.value,
                        securityCode = securityCodeState.value,
                        holderName = holderNameState.value,
                        socialSecurityNumber = socialSecurityNumberState.value,
                        kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumberState.value,
                        kcpCardPassword = kcpCardPasswordState.value,
                        addressInputModel = inputData.address,
                        isStorePaymentSelected = isStoredPaymentMethodEnable,
                        detectedCardTypes = detectedCardTypes,
                        selectedCardIndex = inputData.selectedCardIndex,
                        selectedInstallmentOption = inputData.installmentOption,
                        countryOptions = countryOptions,
                        stateOptions = AddressFormUtils.initializeStateOptions(it)
                    )
                    notifyStateChanged(newOutputData)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun requestCountryList(cardDelegate: NewCardDelegate) {
        viewModelScope.launch {
            val countries = cardDelegate.getCountryList()
            val countryOptions = AddressFormUtils.initializeCountryOptions(cardConfiguration.addressConfiguration, countries)
            countryOptions.firstOrNull { it.selected }?.let {
                inputData.address.country = it.code
                cardDelegate.requestStateList(it.code, viewModelScope)
            }
            with(outputData) {
                this ?: return@with
                val newOutputData = makeOutputData(
                    cardNumber = cardNumberState.value,
                    expiryDate = expiryDateState.value,
                    securityCode = securityCodeState.value,
                    holderName = holderNameState.value,
                    socialSecurityNumber = socialSecurityNumberState.value,
                    kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumberState.value,
                    kcpCardPassword = kcpCardPasswordState.value,
                    addressInputModel = inputData.address,
                    isStorePaymentSelected = isStoredPaymentMethodEnable,
                    detectedCardTypes = this.detectedCardTypes,
                    selectedCardIndex = inputData.selectedCardIndex,
                    selectedInstallmentOption = inputData.installmentOption,
                    countryOptions = countryOptions,
                    stateOptions = stateOptions
                )
                notifyStateChanged(newOutputData)
            }
        }
    }

    private fun makeCvcUIState(cvcPolicy: Brand.FieldPolicy?): InputFieldUIState {
        Logger.d(TAG, "makeCvcUIState: $cvcPolicy")
        return when {
            cardDelegate.isCvcHidden() -> InputFieldUIState.HIDDEN
            // we treat CvcPolicy.HIDDEN as OPTIONAL for now to avoid hiding and showing the cvc field while the user is typing the card number
            cvcPolicy == Brand.FieldPolicy.OPTIONAL || cvcPolicy == Brand.FieldPolicy.HIDDEN -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun makeExpiryDateUIState(expiryDatePolicy: Brand.FieldPolicy?): InputFieldUIState {
        return when (expiryDatePolicy) {
            Brand.FieldPolicy.OPTIONAL, Brand.FieldPolicy.HIDDEN -> InputFieldUIState.OPTIONAL
            else -> InputFieldUIState.REQUIRED
        }
    }

    private fun markSelectedCard(cards: List<DetectedCardType>, selectedIndex: Int): List<DetectedCardType> {
        if (cards.size <= SINGLE_CARD_LIST_SIZE) return cards
        return cards.mapIndexed { index, card ->
            if (index == selectedIndex) {
                card.copy(isSelected = true)
            } else {
                card
            }
        }
    }

    private fun makeInstallmentFieldState(installmentModel: InstallmentModel?): FieldState<InstallmentModel?> {
        return FieldState(installmentModel, Validation.Valid)
    }

    @Suppress("ReturnCount")
    override fun createComponentState(): CardComponentState {
        Logger.v(TAG, "createComponentState")

        // TODO: 29/01/2021 pass outputData as non null parameter
        val stateOutputData = outputData ?: throw CheckoutException("Cannot create state with null outputData")

        val cardNumber = stateOutputData.cardNumberState.value

        val firstCardType = stateOutputData.detectedCardTypes.firstOrNull()?.cardType

        val binValue = cardNumber.take(BIN_VALUE_LENGTH)

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!stateOutputData.isValid || publicKey == null) {
            return CardComponentState(
                paymentComponentData = PaymentComponentData<CardPaymentMethod>(),
                isInputValid = stateOutputData.isValid,
                isReady = publicKey != null,
                cardType = firstCardType,
                binValue = binValue,
                lastFourDigits = null
            )
        }

        val unencryptedCardBuilder = UnencryptedCard.Builder()

        val encryptedCard: EncryptedCard = try {
            if (!isStoredPaymentMethod()) {
                unencryptedCardBuilder.setNumber(stateOutputData.cardNumberState.value)
            }
            if (!cardDelegate.isCvcHidden()) {
                val cvc = stateOutputData.securityCodeState.value
                if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
            }
            val expiryDateResult = stateOutputData.expiryDateState.value
            if (expiryDateResult.expiryYear != ExpiryDate.EMPTY_VALUE && expiryDateResult.expiryMonth != ExpiryDate.EMPTY_VALUE) {
                unencryptedCardBuilder.setExpiryMonth(expiryDateResult.expiryMonth.toString())
                unencryptedCardBuilder.setExpiryYear(expiryDateResult.expiryYear.toString())
            }

            CardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            notifyException(e)
            return CardComponentState(
                paymentComponentData = PaymentComponentData<CardPaymentMethod>(),
                isInputValid = false,
                isReady = true,
                cardType = firstCardType,
                binValue = binValue,
                lastFourDigits = null
            )
        }

        return mapComponentState(
            encryptedCard,
            stateOutputData,
            cardNumber,
            firstCardType,
            binValue
        )
    }

    private fun mapComponentState(
        encryptedCard: EncryptedCard,
        stateOutputData: CardOutputData,
        cardNumber: String,
        firstCardType: CardType?,
        binValue: String
    ): CardComponentState {
        val cardPaymentMethod = CardPaymentMethod()
        cardPaymentMethod.type = CardPaymentMethod.PAYMENT_METHOD_TYPE

        if (!isStoredPaymentMethod()) {
            cardPaymentMethod.encryptedCardNumber = encryptedCard.encryptedCardNumber
            cardPaymentMethod.encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
            cardPaymentMethod.encryptedExpiryYear = encryptedCard.encryptedExpiryYear
        } else {
            cardPaymentMethod.storedPaymentMethodId = (mPaymentMethodDelegate as StoredCardDelegate).getId()
        }

        if (!cardDelegate.isCvcHidden()) {
            cardPaymentMethod.encryptedSecurityCode = encryptedCard.encryptedSecurityCode
        }

        if (cardDelegate.isHolderNameRequired()) {
            cardPaymentMethod.holderName = stateOutputData.holderNameState.value
        }

        if (cardDelegate.isKCPAuthRequired()) {
            publicKey?.let { publicKey ->
                cardPaymentMethod.encryptedPassword = GenericEncrypter.encryptField(
                    GenericEncrypter.KCP_PASSWORD_KEY,
                    stateOutputData.kcpCardPasswordState.value,
                    publicKey
                )
            } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
            cardPaymentMethod.taxNumber = stateOutputData.kcpBirthDateOrTaxNumberState.value
        }

        if (isDualBrandedFlow(stateOutputData)) {
            cardPaymentMethod.brand = stateOutputData.detectedCardTypes.first { it.isSelected }.cardType.txVariant
        }

        cardPaymentMethod.fundingSource = cardDelegate.getFundingSource()

        try {
            cardPaymentMethod.threeDS2SdkVersion = ThreeDS2Service.INSTANCE.sdkVersion
        } catch (e: ClassNotFoundException) {
            Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
        } catch (e: NoClassDefFoundError) {
            Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
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

    fun isStoredPaymentMethod(): Boolean {
        return cardDelegate is StoredCardDelegate
    }

    fun isHolderNameRequired(): Boolean {
        return cardDelegate.isHolderNameRequired()
    }

    fun showStorePaymentField(): Boolean {
        return configuration.isStorePaymentFieldVisible
    }

    @StringRes fun getKcpBirthDateOrTaxNumberHint(input: String): Int {
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
            setStorePaymentMethod(stateOutputData.isStoredPaymentMethodEnable)
            shopperReference = configuration.shopperReference
            if (cardDelegate.isSocialSecurityNumberRequired()) {
                socialSecurityNumber = stateOutputData.socialSecurityNumberState.value
            }
            if (cardDelegate.isAddressRequired(stateOutputData.addressUIState)) {
                billingAddress = AddressFormUtils.makeAddressData(stateOutputData.addressState, stateOutputData.addressUIState)
            }
            if (isInstallmentsRequired(stateOutputData)) {
                installments = InstallmentUtils.makeInstallmentModelObject(stateOutputData.installmentState.value)
            }
        }
    }

    fun isDualBrandedFlow(cardOutputData: CardOutputData): Boolean {
        val reliableDetectedCards = cardOutputData.detectedCardTypes.filter { it.isReliable }
        return reliableDetectedCards.size > 1 && reliableDetectedCards.any { it.isSelected }
    }

    private fun isInstallmentsRequired(cardOutputData: CardOutputData): Boolean {
        return cardOutputData.installmentOptions.isNotEmpty()
    }

    companion object {
        @JvmStatic
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
    }
}
