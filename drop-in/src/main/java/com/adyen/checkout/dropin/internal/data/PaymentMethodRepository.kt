/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface PaymentMethodRepository {

    val paymentMethods: List<PaymentMethod>

    val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>>

    fun removeStoredPaymentMethod(id: String)
}

internal class DefaultPaymentMethodRepository(
    paymentMethods: PaymentMethods,
) : PaymentMethodRepository {

    override val paymentMethods: List<PaymentMethod> = paymentMethods.paymentMethods.orEmpty()

    private val _storedPaymentMethods = MutableStateFlow(paymentMethods.storedPaymentMethods.orEmpty())
    override val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>> = _storedPaymentMethods.asStateFlow()

    override fun removeStoredPaymentMethod(id: String) {
        // TODO - Implement network call and only remove locally if successful
        _storedPaymentMethods.value = _storedPaymentMethods.value.filterNot { it.id == id }
    }
}
