/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.base.component.StoredPaymentMethodDelegate
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class StoredCardDelegate(private val storedPaymentMethod: StoredPaymentMethod) : StoredPaymentMethodDelegate {
    private val logTag = LogUtil.getTag()

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
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
