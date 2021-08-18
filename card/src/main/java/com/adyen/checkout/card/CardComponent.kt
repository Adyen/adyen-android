/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.Address
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
private const val BIN_VALUE_LENGTH = 6
private const val LAST_FOUR_LENGTH = 4

@Suppress("TooManyFunctions")
class CardComponent private constructor(
    private val cardDelegate: CardDelegate,
    cardConfiguration: CardConfiguration
) : BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardComponentState>(cardDelegate, cardConfiguration) {

    private var storedPaymentInputData: CardInputData? = null
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
                            postalCode = postalCodeState.value,
                            isStorePaymentSelected = isStoredPaymentMethodEnable,
                            detectedCardTypes = it
                        )
                        notifyStateChanged(newOutputData)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    constructor(storedCardDelegate: StoredCardDelegate, cardConfiguration: CardConfiguration) : this(
        storedCardDelegate as CardDelegate,
        cardConfiguration
    ) {
        storedPaymentInputData = storedCardDelegate.getStoredCardInputData()

        // TODO: 09/12/2020 move this logic to base component, maybe create the inputdata from the delegate?
        if (!requiresInput()) {
            inputDataChanged(CardInputData())
        }
    }

    constructor(cardDelegate: NewCardDelegate, cardConfiguration: CardConfiguration) : this(
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

        return makeOutputData(
            cardNumber = inputData.cardNumber,
            expiryDate = inputData.expiryDate,
            securityCode = inputData.securityCode,
            holderName = inputData.holderName,
            socialSecurityNumber = inputData.socialSecurityNumber,
            kcpBirthDateOrTaxNumber = inputData.kcpBirthDateOrTaxNumber,
            kcpCardPassword = inputData.kcpCardPassword,
            isStorePaymentSelected = inputData.isStorePaymentSelected,
            postalCode = inputData.postalCode,
            detectedCardTypes = detectedCardTypes
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
        isStorePaymentSelected: Boolean,
        postalCode: String,
        detectedCardTypes: List<DetectedCardType>
    ): CardOutputData {
        val firstDetectedType = detectedCardTypes.firstOrNull()
        return CardOutputData(
            cardDelegate.validateCardNumber(cardNumber, firstDetectedType?.enableLuhnCheck),
            cardDelegate.validateExpiryDate(expiryDate),
            cardDelegate.validateSecurityCode(securityCode, firstDetectedType),
            cardDelegate.validateHolderName(holderName),
            cardDelegate.validateSocialSecurityNumber(socialSecurityNumber),
            cardDelegate.validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber),
            cardDelegate.validateKcpCardPassword(kcpCardPassword),
            cardDelegate.validatePostalCode(postalCode),
            isStorePaymentSelected,
            makeCvcUIState(firstDetectedType?.cvcPolicy),
            detectedCardTypes,
            cardDelegate.isSocialSecurityNumberRequired(),
            cardDelegate.isKCPAuthRequired()
        )
    }

    private fun makeCvcUIState(cvcPolicy: Brand.CvcPolicy?): CvcUIState {
        Logger.d(TAG, "makeCvcUIState: $cvcPolicy")
        return when {
            cardDelegate.isCvcHidden() -> CvcUIState.HIDDEN
            // we treat CvcPolicy.HIDDEN as OPTIONAL for now to avoid hiding and showing the cvc field while the user is typing the card number
            cvcPolicy == Brand.CvcPolicy.OPTIONAL || cvcPolicy == Brand.CvcPolicy.HIDDEN -> CvcUIState.OPTIONAL
            else -> CvcUIState.REQUIRED
        }
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

        val paymentComponentData = PaymentComponentData<CardPaymentMethod>().apply {
            paymentMethod = cardPaymentMethod
            setStorePaymentMethod(stateOutputData.isStoredPaymentMethodEnable)
            shopperReference = configuration.shopperReference

            if (cardDelegate.isSocialSecurityNumberRequired()) {
                socialSecurityNumber = stateOutputData.socialSecurityNumberState.value
            }

            if (cardDelegate.isPostalCodeRequired()) {
                billingAddress = makeAddressData(stateOutputData)
            }
        }

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

    fun getStoredPaymentInputData(): CardInputData? {
        return storedPaymentInputData
    }

    fun isHolderNameRequired(): Boolean {
        return cardDelegate.isHolderNameRequired()
    }

    fun showStorePaymentField(): Boolean {
        return configuration.isShowStorePaymentFieldEnable
    }

    fun makeAddressData(outputData: CardOutputData): Address {
        return Address().apply {
            postalCode = outputData.postalCodeState.value
            street = Address.ADDRESS_NULL_PLACEHOLDER
            stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER
            houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER
            city = Address.ADDRESS_NULL_PLACEHOLDER
            country = Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER
        }
    }

    @StringRes fun getKcpBirthDateOrTaxNumberHint(input: String): Int {
        return when {
            input.length > KcpValidationUtils.KCP_BIRTH_DATE_LENGTH -> R.string.checkout_kcp_tax_number_hint
            else -> R.string.checkout_kcp_birth_date_or_tax_number_hint
        }
    }

    companion object {
        @JvmStatic
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()
    }
}
