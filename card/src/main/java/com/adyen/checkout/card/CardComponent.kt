/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import android.text.TextUtils
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.components.validation.ValidatedField
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.Card
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.Encryptor
import java.util.ArrayList
import java.util.Collections

private val TAG = LogUtil.getTag()

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
private const val BIN_VALUE_LENGTH = 6
private val NO_CVC_BRANDS: Set<CardType> = hashSetOf(CardType.BCMC)

@Suppress("TooManyFunctions")
class CardComponent private constructor(
    cardDelegate: PaymentMethodDelegate,
    cardConfiguration: CardConfiguration
) : BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardComponentState>(cardDelegate, cardConfiguration) {

    var filteredSupportedCards: List<CardType> = emptyList()
        private set
    private var storedPaymentInputData: CardInputData? = null

    @Suppress("unused")
    constructor(storedCardDelegate: StoredCardDelegate, cardConfiguration: CardConfiguration) : this(
        storedCardDelegate as PaymentMethodDelegate,
        cardConfiguration
    ) {
        storedPaymentInputData = storedCardDelegate.getStoredCardInputData()

        val cardType = storedCardDelegate.getCardType()
        if (cardType != null) {
            val storedCardType: MutableList<CardType> = ArrayList()
            storedCardType.add(cardType)
            filteredSupportedCards = Collections.unmodifiableList(storedCardType)
        }

        // TODO: 09/12/2020 move this logic to base component, maybe create the inputdata from the delegate?
        if (!requiresInput()) {
            inputDataChanged(CardInputData())
        }
    }

    @Suppress("unused")
    constructor(cardDelegate: CardDelegate, cardConfiguration: CardConfiguration) : this(
        cardDelegate as PaymentMethodDelegate,
        cardConfiguration
    )

    override fun requiresInput(): Boolean {
        return !(isStoredPaymentMethod() && mConfiguration.isHideCvcStoredCard)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> {
        return PAYMENT_METHOD_TYPES
    }

    override fun onInputDataChanged(inputData: CardInputData): CardOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return CardOutputData(
            validateCardNumber(inputData.cardNumber),
            validateExpiryDate(inputData.expiryDate),
            validateSecurityCode(inputData.securityCode),
            validateHolderName(inputData.holderName),
            inputData.isStorePaymentEnable,
            isCvcHidden()
        )
    }

    @Suppress("ReturnCount")
    override fun createComponentState(): CardComponentState {
        Logger.v(TAG, "createComponentState")

        val cardPaymentMethod = CardPaymentMethod()
        cardPaymentMethod.type = CardPaymentMethod.PAYMENT_METHOD_TYPE

        val card = Card.Builder()
        val outputData = outputData
        val paymentComponentData = PaymentComponentData<CardPaymentMethod>()

        val cardNumber = outputData!!.cardNumberField.value

        val firstCardType: CardType? = if (filteredSupportedCards.isNotEmpty()) filteredSupportedCards[0] else null

        val binValue: String = getBinValueFromCardNumber(cardNumber)

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid) {
            return CardComponentState(paymentComponentData, false, firstCardType, binValue)
        }

        val encryptedCard: EncryptedCard
        encryptedCard = try {
            if (!isStoredPaymentMethod()) {
                card.setNumber(outputData.cardNumberField.value)
            }
            if (!isCvcHidden()) {
                card.setSecurityCode(outputData.securityCodeField.value)
            }
            val expiryDateResult = outputData.expiryDateField.value
            if (expiryDateResult.expiryYear != ExpiryDate.EMPTY_VALUE && expiryDateResult.expiryMonth != ExpiryDate.EMPTY_VALUE) {
                card.setExpiryDate(expiryDateResult.expiryMonth, expiryDateResult.expiryYear)
            }
            Encryptor.INSTANCE.encryptFields(card.build(), configuration.publicKey)
        } catch (e: EncryptionException) {
            notifyException(e)
            return CardComponentState(paymentComponentData, false, firstCardType, binValue)
        }

        if (!isStoredPaymentMethod()) {
            cardPaymentMethod.encryptedCardNumber = encryptedCard.encryptedNumber
            cardPaymentMethod.encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
            cardPaymentMethod.encryptedExpiryYear = encryptedCard.encryptedExpiryYear
        } else {
            cardPaymentMethod.storedPaymentMethodId = (mPaymentMethodDelegate as StoredCardDelegate).getId()
        }

        if (!isCvcHidden()) {
            cardPaymentMethod.encryptedSecurityCode = encryptedCard.encryptedSecurityCode
        }

        if (isHolderNameRequire()) {
            cardPaymentMethod.holderName = outputData.holderNameField.value
        }

        paymentComponentData.paymentMethod = cardPaymentMethod
        paymentComponentData.setStorePaymentMethod(outputData.isStoredPaymentMethodEnable)
        paymentComponentData.shopperReference = configuration.shopperReference

        return CardComponentState(paymentComponentData, outputData.isValid, firstCardType, binValue)
    }

    fun isStoredPaymentMethod(): Boolean {
        return storedPaymentInputData != null
    }

    fun getStoredPaymentInputData(): CardInputData? {
        return storedPaymentInputData
    }

    fun isHolderNameRequire(): Boolean {
        return if (isStoredPaymentMethod()) {
            false
        } else {
            configuration.isHolderNameRequire
        }
    }

    fun showStorePaymentField(): Boolean {
        return configuration.isShowStorePaymentFieldEnable
    }

    private fun validateCardNumber(cardNumber: String): ValidatedField<String> {
        return if (isStoredPaymentMethod()) {
            ValidatedField(cardNumber, ValidatedField.Validation.VALID)
        } else {
            filteredSupportedCards = updateSupportedFilterCards(cardNumber)
            CardValidationUtils.validateCardNumber(cardNumber)
        }
    }

    private fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate> {
        return if (isStoredPaymentMethod()) {
            ValidatedField(expiryDate, ValidatedField.Validation.VALID)
        } else {
            CardValidationUtils.validateExpiryDate(expiryDate)
        }
    }

    private fun validateSecurityCode(securityCode: String): ValidatedField<String> {
        return if (isCvcHidden()) {
            ValidatedField(securityCode, ValidatedField.Validation.VALID)
        } else {
            val firstCardType: CardType? = if (filteredSupportedCards.isNotEmpty()) filteredSupportedCards[0] else null
            CardValidationUtils.validateSecurityCode(securityCode, firstCardType)
        }
    }

    private fun isCvcHidden(): Boolean {
        return if (isStoredPaymentMethod()) {
            configuration.isHideCvcStoredCard || isBrandWithoutCvc((mPaymentMethodDelegate as StoredCardDelegate).getCardType()!!)
        } else {
            configuration.isHideCvc
        }
    }

    private fun isBrandWithoutCvc(cardType: CardType): Boolean {
        return NO_CVC_BRANDS.contains(cardType)
    }

    private fun validateHolderName(holderName: String): ValidatedField<String> {
        return if (isHolderNameRequire() && TextUtils.isEmpty(holderName)) {
            ValidatedField(holderName, ValidatedField.Validation.INVALID)
        } else {
            ValidatedField(holderName, ValidatedField.Validation.VALID)
        }
    }

    private fun updateSupportedFilterCards(cardNumber: String?): List<CardType> {
        Logger.d(TAG, "updateSupportedFilterCards")
        if (cardNumber.isNullOrEmpty()) {
            return emptyList()
        }
        val supportedCardTypes = configuration.supportedCardTypes
        val estimateCardTypes = CardType.estimate(cardNumber)
        val filteredCards: MutableList<CardType> = ArrayList()
        for (supportedCard in supportedCardTypes) {
            if (estimateCardTypes.contains(supportedCard)) {
                filteredCards.add(supportedCard)
            }
        }
        return Collections.unmodifiableList(filteredCards)
    }

    private fun getBinValueFromCardNumber(cardNumber: String): String {
        return if (cardNumber.length < BIN_VALUE_LENGTH) cardNumber else cardNumber.substring(0, BIN_VALUE_LENGTH)
    }

    companion object {
        @JvmStatic
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()
    }
}
