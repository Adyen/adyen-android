/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.components.CheckoutTarget
import java.util.WeakHashMap

/**
 * Saves the [CheckoutFlowPhase] of the flow for [target] in [savedStateHandle].
 *
 * Each target is saved under its own key, so flows for different targets can share the same [savedStateHandle]. Use a
 * `null` [target] for flows without one, such as action-only flows.
 *
 * A saved phase is only restored once per [savedStateHandle] instance. A new instance comes from saved state, after
 * process death or when its view model is recreated, so the first flow on it continues where the previous one stopped.
 * A later flow for the same target on the same instance replaces a flow that was abandoned in this process, for example
 * when the shopper retries after leaving a redirect, so it starts fresh instead.
 */
internal class CheckoutFlowStateStore(
    private val savedStateHandle: SavedStateHandle,
    target: CheckoutTarget?,
) {

    private val key: String = "$SAVED_STATE_KEY:${target?.toTargetId() ?: ACTION_ONLY_TARGET_ID}"

    fun restore(): CheckoutFlowPhase? {
        if (!markAsRestored()) {
            clear()
            return null
        }
        return savedStateHandle[key]
    }

    fun save(phase: CheckoutFlowPhase) {
        savedStateHandle[key] = phase
    }

    fun clear() {
        savedStateHandle.remove<CheckoutFlowPhase>(key)
    }

    private fun markAsRestored(): Boolean = synchronized(restoredKeys) {
        restoredKeys.getOrPut(savedStateHandle) { mutableSetOf() }.add(key)
    }

    // This is the only place that identifies a target in saved state.
    private fun CheckoutTarget.toTargetId(): String = when (this) {
        // TODO - replace type with the id of the payment method once it's implemented. This would allow multiple
        //  controllers of the same type.
        is CheckoutTarget.PaymentMethod -> "payment_method:$type"
        is CheckoutTarget.StoredPaymentMethod -> "stored_payment_method:$id"
        else -> toString()
    }

    companion object {
        private const val SAVED_STATE_KEY = "adyen_checkout_flow_state"
        private const val ACTION_ONLY_TARGET_ID = "action_only"

        // Keys already restored in this process, per saved state handle instance. Held weakly, so handles of cleared
        // view models are not kept alive.
        private val restoredKeys = WeakHashMap<SavedStateHandle, MutableSet<String>>()
    }
}
