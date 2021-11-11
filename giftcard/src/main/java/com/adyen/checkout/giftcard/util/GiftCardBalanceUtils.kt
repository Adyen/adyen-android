/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/9/2021.
 */

package com.adyen.checkout.giftcard.util

import com.adyen.checkout.components.model.payments.Amount
import kotlin.math.min

// TODO docs
object GiftCardBalanceUtils {

    // TODO docs
    fun checkBalance(balance: Amount, transactionLimit: Amount?, amountToBePaid: Amount): GiftCardBalanceStatus {
        return when {
            amountToBePaid.isEmpty || amountToBePaid.value <= 0 -> GiftCardBalanceStatus.ZeroAmountToBePaid
            balance.isEmpty || balance.value <= 0 -> GiftCardBalanceStatus.ZeroBalance
            amountToBePaid.currency != balance.currency -> GiftCardBalanceStatus.NonMatchingCurrencies
            transactionLimit != null && amountToBePaid.currency != transactionLimit.currency -> GiftCardBalanceStatus.NonMatchingCurrencies
            else -> calculateRemainingAmount(balance, transactionLimit, amountToBePaid)
        }
    }

    private fun calculateRemainingAmount(balance: Amount, transactionLimit: Amount?, amountToBePaid: Amount): GiftCardBalanceStatus {
        val maxPayableAmount =
            if (transactionLimit == null || transactionLimit.isEmpty) balance.value
            else min(balance.value, transactionLimit.value)

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

sealed class GiftCardBalanceStatus {
    class FullPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardBalanceStatus()
    class PartialPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardBalanceStatus()
    object NonMatchingCurrencies : GiftCardBalanceStatus()
    object ZeroAmountToBePaid : GiftCardBalanceStatus()
    object ZeroBalance : GiftCardBalanceStatus()
}
