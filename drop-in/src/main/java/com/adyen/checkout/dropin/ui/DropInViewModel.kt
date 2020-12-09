/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import androidx.lifecycle.ViewModel
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.dropin.DropInConfiguration

class DropInViewModel(
    val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    val dropInConfiguration: DropInConfiguration
) : ViewModel() {

    val showPreselectedStored = paymentMethodsApiResponse.storedPaymentMethods?.isNotEmpty() ?: false
    val preselectedStoredPayment = paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull{
        it.isEcommerce && PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type)
    } ?: StoredPaymentMethod()

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun getPaymentMethod(type: String): PaymentMethod {
        return paymentMethodsApiResponse.paymentMethods?.firstOrNull { it.type == type } ?: PaymentMethod()
    }
}
