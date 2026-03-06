/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class TestPaymentMethodRepository(
    storedMethods: List<StoredPaymentMethod> = emptyList(),
    override val paymentMethods: List<PaymentMethod> = emptyList(),
) : PaymentMethodRepository {

    private val _storedPaymentMethods = MutableStateFlow(storedMethods)
    override val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>> = _storedPaymentMethods

    override fun removeStoredPaymentMethod(id: String) {
        _storedPaymentMethods.value = _storedPaymentMethods.value.filterNot { it.id == id }
    }
}
