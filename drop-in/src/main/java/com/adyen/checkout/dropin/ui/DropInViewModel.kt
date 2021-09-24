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
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayComponent

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

    fun shouldSkipToSinglePaymentMethod(): Boolean {
        val noStored = paymentMethodsApiResponse.storedPaymentMethods.isNullOrEmpty()
        val singlePm = paymentMethodsApiResponse.paymentMethods?.size == 1

        val firstPaymentMethod = paymentMethodsApiResponse.paymentMethods?.get(0)
        val paymentMethodHasComponent = PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(firstPaymentMethod?.type) &&
                !GooglePayComponent.PAYMENT_METHOD_TYPES.contains(firstPaymentMethod?.type)

        return noStored && singlePm && paymentMethodHasComponent && dropInConfiguration.skipListWhenSinglePaymentMethod
    }
}
