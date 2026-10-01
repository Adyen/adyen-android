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

/**
 * Saves the [CheckoutFlowPhase] of the flow for [target] in [savedStateHandle].
 *
 * Each target is saved under its own key, so flows for different targets can share the same [savedStateHandle]. Use a
 * `null` [target] for flows without one, such as action-only flows.
 */
internal class CheckoutFlowStateStore(
    private val savedStateHandle: SavedStateHandle,
    target: CheckoutTarget?,
) {

    private val key: String = "$SAVED_STATE_KEY:${target?.toTargetId() ?: ACTION_ONLY_TARGET_ID}"

    fun restore(): CheckoutFlowPhase? = savedStateHandle[key]

    fun save(phase: CheckoutFlowPhase) {
        savedStateHandle[key] = phase
    }

    fun clear() {
        savedStateHandle.remove<CheckoutFlowPhase>(key)
    }

    // This is the only place that identifies a target in saved state.
    private fun CheckoutTarget.toTargetId(): String = when (this) {
        is CheckoutTarget.PaymentMethod -> "payment_method:$type"
        is CheckoutTarget.StoredPaymentMethod -> "stored_payment_method:$id"
        else -> toString()
    }

    companion object {
        private const val SAVED_STATE_KEY = "adyen_checkout_flow_state"
        private const val ACTION_ONLY_TARGET_ID = "action_only"
    }
}
