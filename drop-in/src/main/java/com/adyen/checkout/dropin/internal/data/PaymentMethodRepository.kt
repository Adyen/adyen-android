/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal interface PaymentMethodRepository {

    val paymentMethods: List<PaymentMethod>

    val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>>

    suspend fun removeStoredPaymentMethod(id: String): Result<Unit>
}

internal class DefaultPaymentMethodRepository(
    override val paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    private val storedPaymentMethodRemover: StoredPaymentMethodRemover,
    private val coroutineScope: CoroutineScope,
) : PaymentMethodRepository {

    private val _storedPaymentMethods = MutableStateFlow(storedPaymentMethods)
    override val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>> = _storedPaymentMethods.asStateFlow()

    private val ongoingRemovals = mutableMapOf<String, Deferred<Result<Unit>>>()

    /**
     * Cancelling the caller only stops the waiting: the removal itself keeps running, so the list and the session
     * data stay in line with the backend. A removal requested while the same item is already being removed shares
     * that removal's result instead of calling the backend again.
     */
    override suspend fun removeStoredPaymentMethod(id: String): Result<Unit> {
        val storedPaymentMethod = _storedPaymentMethods.value.firstOrNull { it.id == id }
            ?: return Result.failure(IllegalArgumentException("No stored payment method found with id $id"))

        val removal = synchronized(ongoingRemovals) {
            // Started lazily, so that the removal is registered before it can finish and unregister itself.
            ongoingRemovals.getOrPut(id) {
                coroutineScope.async(start = CoroutineStart.LAZY) { remove(storedPaymentMethod) }
            }
        }
        return removal.await()
    }

    private suspend fun remove(storedPaymentMethod: StoredPaymentMethod): Result<Unit> = try {
        runSuspendCatching { storedPaymentMethodRemover.remove(storedPaymentMethod).getOrThrow() }.onSuccess {
            _storedPaymentMethods.update { current -> current.filterNot { it.id == storedPaymentMethod.id } }
        }
    } finally {
        synchronized(ongoingRemovals) { ongoingRemovals.remove(storedPaymentMethod.id) }
    }
}
