/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/9/2021.
 */

package com.adyen.checkout.giftcard.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.util.isEmpty
import kotlin.math.min

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object GiftCardBalanceUtils {

    /**
     * Check whether a full or partial payment can be made with a certain gift card for a give amount.
     *
     * @param balance the balance of the gift card. Should be extracted from the /paymentMethods/balance call.
     * @param transactionLimit the maximum amount for a single transaction of the gift card. Should be extracted
     * from the /paymentMethods/balance call.
     * @param amountToBePaid the desired amount to be paid using this gift card.
     * @return the result of the balance check.
     */
    fun checkBalance(balance: Amount?, transactionLimit: Amount?, amountToBePaid: Amount?): GiftCardBalanceStatus {
        return when {
            amountToBePaid == null || amountToBePaid.value <= 0 -> GiftCardBalanceStatus.ZeroAmountToBePaid
            balance == null || balance.value <= 0 -> GiftCardBalanceStatus.ZeroBalance
            amountToBePaid.currency != balance.currency -> GiftCardBalanceStatus.NonMatchingCurrencies
            transactionLimit != null && amountToBePaid.currency != transactionLimit.currency ->
                GiftCardBalanceStatus.NonMatchingCurrencies
            else -> calculateRemainingAmount(balance, transactionLimit, amountToBePaid)
        }
    }

    private fun calculateRemainingAmount(
        balance: Amount,
        transactionLimit: Amount?,
        amountToBePaid: Amount
    ): GiftCardBalanceStatus {
        val maxPayableAmount = if (transactionLimit == null || transactionLimit.isEmpty) {
            balance.value
        } else {
            min(balance.value, transactionLimit.value)
        }

        val paidCurrency = amountToBePaid.currency
        val actualPaidAmount = min(maxPayableAmount, amountToBePaid.value)

        val amountPaid = Amount().apply {
            currency = paidCurrency
            value = actualPaidAmount
        }
        val remainingBalance = Amount().apply {
            currency = paidCurrency
            value = balance.value - actualPaidAmount
        }

        return if (maxPayableAmount >= amountToBePaid.value) {
            GiftCardBalanceStatus.FullPayment(amountPaid = amountPaid, remainingBalance = remainingBalance)
        } else {
            GiftCardBalanceStatus.PartialPayment(amountPaid = amountPaid, remainingBalance = remainingBalance)
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class GiftCardBalanceStatus {
    class FullPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardBalanceStatus()
    class PartialPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardBalanceStatus()
    object NonMatchingCurrencies : GiftCardBalanceStatus()
    object ZeroAmountToBePaid : GiftCardBalanceStatus()
    object ZeroBalance : GiftCardBalanceStatus()
}
