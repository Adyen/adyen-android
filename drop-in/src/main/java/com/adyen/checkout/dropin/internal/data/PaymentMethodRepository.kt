/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal interface PaymentMethodRepository {

    val paymentMethods: List<PaymentMethod>

    val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>>

    /**
     * Removes the stored payment method on the backend, and only takes it off [storedPaymentMethods] if that succeeds.
     */
    suspend fun removeStoredPaymentMethod(id: String): Result<Unit>
}

internal class DefaultPaymentMethodRepository(
    override val paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    private val storedPaymentMethodRemover: StoredPaymentMethodRemover,
) : PaymentMethodRepository {

    private val _storedPaymentMethods = MutableStateFlow(storedPaymentMethods)
    override val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>> = _storedPaymentMethods.asStateFlow()

    override suspend fun removeStoredPaymentMethod(id: String): Result<Unit> {
        val storedPaymentMethod = _storedPaymentMethods.value.firstOrNull { it.id == id }
            ?: return Result.failure(IllegalArgumentException("No stored payment method found with id $id"))

        return storedPaymentMethodRemover.remove(storedPaymentMethod).onSuccess {
            _storedPaymentMethods.update { current -> current.filterNot { it.id == id } }
        }
    }
}
