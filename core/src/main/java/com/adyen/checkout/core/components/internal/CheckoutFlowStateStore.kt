/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2026.
 */

package com.adyen.checkout.core.components.internal

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.components.CheckoutTarget
import kotlinx.parcelize.Parcelize

/**
 * Saves the [CheckoutFlowPhase] of the flow for [target] in [savedStateHandle].
 *
 * Saved state belongs to the target it was saved for. A store for another target does not restore it and clears it
 * instead. Use a `null` [target] for flows without one, such as action-only flows.
 */
internal class CheckoutFlowStateStore(
    private val savedStateHandle: SavedStateHandle,
    target: CheckoutTarget?,
) {

    private val targetId: String? = target?.toTargetId()

    fun restore(): CheckoutFlowPhase? {
        val savedState = savedStateHandle.get<CheckoutFlowSavedState>(SAVED_STATE_KEY)
        if (savedState != null && savedState.targetId != targetId) clear()
        return savedState?.takeIf { it.targetId == targetId }?.phase
    }

    fun save(phase: CheckoutFlowPhase) {
        savedStateHandle[SAVED_STATE_KEY] = CheckoutFlowSavedState(targetId = targetId, phase = phase)
    }

    fun clear() {
        savedStateHandle.remove<CheckoutFlowSavedState>(SAVED_STATE_KEY)
    }

    // This is the only place that identifies a target in saved state.
    private fun CheckoutTarget.toTargetId(): String = when (this) {
        is CheckoutTarget.PaymentMethod -> "payment_method:$type"
        is CheckoutTarget.StoredPaymentMethod -> "stored_payment_method:$id"
        else -> toString()
    }

    @Parcelize
    internal data class CheckoutFlowSavedState(
        val targetId: String?,
        val phase: CheckoutFlowPhase,
    ) : Parcelable

    companion object {
        private const val SAVED_STATE_KEY = "adyen_checkout_flow_state"
    }
}
