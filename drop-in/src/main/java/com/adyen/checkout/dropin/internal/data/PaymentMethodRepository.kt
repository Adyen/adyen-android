/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal interface PaymentMethodRepository {

    val paymentMethods: List<PaymentMethod>

    val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>>

    /**
     * The ids of the stored payment methods whose removal is still running. Removals outlive the screen that started
     * them, so a screen reads this instead of tracking them itself.
     */
    val storedPaymentMethodIdsBeingRemoved: StateFlow<Set<String>>

    /**
     * The ids of the stored payment methods whose removal failed, for the same reason as
     * [storedPaymentMethodIdsBeingRemoved]. A failure is not replayed, so it only reaches screens that are open.
     */
    val storedPaymentMethodRemovalFailures: SharedFlow<String>

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

    private val _storedPaymentMethodIdsBeingRemoved = MutableStateFlow<Set<String>>(emptySet())
    override val storedPaymentMethodIdsBeingRemoved: StateFlow<Set<String>> =
        _storedPaymentMethodIdsBeingRemoved.asStateFlow()

    private val _storedPaymentMethodRemovalFailures = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val storedPaymentMethodRemovalFailures: SharedFlow<String> =
        _storedPaymentMethodRemovalFailures.asSharedFlow()

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
            }.also { _storedPaymentMethodIdsBeingRemoved.value = ongoingRemovals.keys.toSet() }
        }
        return removal.await()
    }

    private suspend fun remove(storedPaymentMethod: StoredPaymentMethod): Result<Unit> = try {
        runSuspendCatching { storedPaymentMethodRemover.remove(storedPaymentMethod).getOrThrow() }
            .onSuccess {
                _storedPaymentMethods.update { current -> current.filterNot { it.id == storedPaymentMethod.id } }
            }
            .onFailure { e ->
                adyenLog(AdyenLogLevel.ERROR, e) { "Failed to remove the stored payment method." }
                _storedPaymentMethodRemovalFailures.tryEmit(storedPaymentMethod.id)
            }
    } finally {
        synchronized(ongoingRemovals) {
            ongoingRemovals.remove(storedPaymentMethod.id)
            _storedPaymentMethodIdsBeingRemoved.value = ongoingRemovals.keys.toSet()
        }
    }
}
