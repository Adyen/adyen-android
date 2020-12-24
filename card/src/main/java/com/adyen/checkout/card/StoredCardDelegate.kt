/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.components.validation.ValidatedField
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val NO_CVC_BRANDS: Set<CardType> = hashSetOf(CardType.BCMC)

class StoredCardDelegate(
    private val storedPaymentMethod: StoredPaymentMethod,
    cardConfiguration: CardConfiguration
) : CardDelegate(cardConfiguration) {
    private val logTag = LogUtil.getTag()

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun validateCardNumber(cardNumber: String): ValidatedField<String> {
        return ValidatedField(cardNumber, ValidatedField.Validation.VALID)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate> {
        return ValidatedField(expiryDate, ValidatedField.Validation.VALID)
    }

    override fun validateSecurityCode(securityCode: String, cardType: CardType?): ValidatedField<String> {
        return if (cardConfiguration.isHideCvcStoredCard || NO_CVC_BRANDS.contains(cardType)) {
            ValidatedField(securityCode, ValidatedField.Validation.VALID)
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    override fun isCvcHidden(): Boolean {
        return cardConfiguration.isHideCvcStoredCard || NO_CVC_BRANDS.contains(getCardType())
    }

    override fun requiresInput(): Boolean {
        return !cardConfiguration.isHideCvcStoredCard
    }

    override fun isHolderNameRequired(): Boolean {
        return false
    }

    fun getStoredCardInputData(): CardInputData {
        val storedCardInputData = CardInputData()
        storedCardInputData.cardNumber = storedPaymentMethod.lastFour.orEmpty()

        try {
            val storedDate = ExpiryDate(storedPaymentMethod.expiryMonth.orEmpty().toInt(), storedPaymentMethod.expiryYear.orEmpty().toInt())
            storedCardInputData.expiryDate = storedDate
        } catch (e: NumberFormatException) {
            Logger.e(logTag, "Failed to parse stored Date", e)
            storedCardInputData.expiryDate = ExpiryDate.EMPTY_DATE
        }

        return storedCardInputData
    }

    fun getCardType(): CardType? {
        return CardType.getByBrandName(storedPaymentMethod.brand.orEmpty())
    }

    fun getId(): String {
        return storedPaymentMethod.id ?: "ID_NOT_FOUND"
    }
}
