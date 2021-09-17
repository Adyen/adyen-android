/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.dropin.DropInConfiguration

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

    fun getPaymentMethod(type: String): PaymentMethod {
        return paymentMethodsApiResponse.paymentMethods?.firstOrNull { it.type == type } ?: PaymentMethod()
    }

    fun skipPaymentMethodDialog(): Boolean {
        return dropInConfiguration.skipPaymentMethodsDialogWhenOnlyOnePaymentMethodIsAvailable && hasOnlyOnePaymentMethod()
    }

    private fun hasOnlyOnePaymentMethod(): Boolean {
        val paymentMethodsSize = paymentMethodsApiResponse.paymentMethods?.size ?: 0
        val storedPaymentMethodsSize = paymentMethodsApiResponse.storedPaymentMethods?.size ?: 0
        return paymentMethodsSize + storedPaymentMethodsSize == 1
    }

    fun getOneAndOnlyPaymentMethodType(): String {
        if (!hasOnlyOnePaymentMethod()) {
            return ""
        }
        return when {
            !paymentMethodsApiResponse.paymentMethods.isNullOrEmpty() -> {
                paymentMethodsApiResponse.paymentMethods!!.first().type.orEmpty()
            }
            !paymentMethodsApiResponse.storedPaymentMethods.isNullOrEmpty() -> {
                paymentMethodsApiResponse.storedPaymentMethods!!.first().type.orEmpty()
            }
            else -> {
                ""
            }
        }
    }
}
