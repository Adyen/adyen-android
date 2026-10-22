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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal class TestPaymentMethodRepository(
    storedMethods: List<StoredPaymentMethod> = emptyList(),
    override val paymentMethods: List<PaymentMethod> = emptyList(),
) : PaymentMethodRepository {

    private val _storedPaymentMethods = MutableStateFlow(storedMethods)
    override val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>> = _storedPaymentMethods

    override val storedPaymentMethodIdsBeingRemoved: StateFlow<Set<String>> = MutableStateFlow(emptySet())

    override val storedPaymentMethodRemovalFailures: SharedFlow<String> = MutableSharedFlow()

    override suspend fun removeStoredPaymentMethod(id: String): Result<Unit> {
        val current = _storedPaymentMethods.value
        if (current.none { it.id == id }) {
            return Result.failure(IllegalArgumentException("No stored payment method found with id $id"))
        }
        _storedPaymentMethods.value = current.filterNot { it.id == id }
        return Result.success(Unit)
    }
}
