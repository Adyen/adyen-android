/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.service.BalanceResult
import com.adyen.checkout.giftcard.util.GiftCardBalanceResult
import com.adyen.checkout.giftcard.util.GiftCardBalanceUtils
import org.json.JSONObject

private val TAG = LogUtil.getTag()

class DropInViewModel(
    val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    val dropInConfiguration: DropInConfiguration,
    val resultHandlerIntent: Intent?
) : ViewModel() {

    val showPreselectedStored = paymentMethodsApiResponse.storedPaymentMethods?.any { it.isEcommerce } == true &&
        dropInConfiguration.showPreselectedStoredPaymentMethod
    val preselectedStoredPayment = paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull {
        it.isEcommerce && PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type)
    } ?: StoredPaymentMethod()

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun handleBalanceResult(balanceResult: BalanceResult): GiftCardResult {
        Logger.d(TAG, "handleBalanceResult - balance: ${balanceResult.balance} - transactionLimit: ${balanceResult.transactionLimit}")
        val balance = Amount.SERIALIZER.deserialize(JSONObject(balanceResult.balance))
        val transactionLimit =
            if (balanceResult.transactionLimit == null) null
            else Amount.SERIALIZER.deserialize(JSONObject(balanceResult.transactionLimit))
        val giftCardBalanceResult = GiftCardBalanceUtils.checkBalance(
            balance = balance,
            transactionLimit = transactionLimit,
            amountToBePaid = dropInConfiguration.amount
        )
        return when (giftCardBalanceResult) {
            is GiftCardBalanceResult.ZeroBalance -> {
                Logger.i(TAG, "handleBalanceResult - Gift Card has zero balance")
                GiftCardResult.Error(R.string.checkout_giftcard_error_zero_balance, "Gift Card has zero balance", false)
            }
            is GiftCardBalanceResult.NonMatchingCurrencies -> {
                Logger.e(TAG, "handleBalanceResult - Gift Card currency mismatch")
                GiftCardResult.Error(R.string.checkout_giftcard_error_currency, "Gift Card currency mismatch", false)
            }
            is GiftCardBalanceResult.ZeroAmountToBePaid -> {
                Logger.e(TAG, "handleBalanceResult - You must set an amount in DropInConfiguration.Builder to enable gift card payments")
                GiftCardResult.Error(R.string.payment_failed, "Drop-in amount is not set", true)
            }
            is GiftCardBalanceResult.FullPayment -> {
                GiftCardResult.FullPayment(giftCardBalanceResult.amountPaid, giftCardBalanceResult.remainingBalance)
            }
            is GiftCardBalanceResult.PartialPayment -> {
                GiftCardResult.PartialPayment(giftCardBalanceResult.amountPaid, giftCardBalanceResult.remainingBalance)
            }
        }
    }
}

sealed class GiftCardResult {
    class FullPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardResult()
    class PartialPayment(val amountPaid: Amount, val remainingBalance: Amount) : GiftCardResult()
    class Error(@StringRes val errorMessage: Int, val reason: String, val terminateDropIn: Boolean) : GiftCardResult()
}
