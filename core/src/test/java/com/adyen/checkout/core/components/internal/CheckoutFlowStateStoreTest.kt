/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.components.CheckoutTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class CheckoutFlowStateStoreTest {

    private val savedStateHandle = SavedStateHandle()

    @Test
    fun `when nothing was saved, then restore returns null`() {
        val store = createStore()

        assertNull(store.restore())
    }

    @Test
    fun `when a phase is saved, then a store on the rebuilt handle restores it`() {
        val action = TestAction(type = "redirect", paymentData = "test_payment_data")
        createStore().save(CheckoutFlowPhase.HandlingAction(action))

        val restoredPhase = createRestoredStore().restore()

        assertEquals(CheckoutFlowPhase.HandlingAction(action), restoredPhase)
    }

    @Test
    fun `when a phase is saved again, then the latest phase is restored`() {
        val store = createStore()
        store.save(CheckoutFlowPhase.Submitted)

        store.save(CheckoutFlowPhase.Input)

        assertEquals(CheckoutFlowPhase.Input, createRestoredStore().restore())
    }

    @Test
    fun `when the saved state is cleared, then restore returns null`() {
        val store = createStore()
        store.save(CheckoutFlowPhase.Submitted)

        store.clear()

        assertNull(createRestoredStore().restore())
    }

    @Test
    fun `when the saved state belongs to another target, then restore returns null`() {
        createStore(target = CheckoutTarget.PaymentMethod("ideal")).save(CheckoutFlowPhase.Submitted)

        val restoredPhase = createRestoredStore(target = CheckoutTarget.PaymentMethod("scheme")).restore()

        assertNull(restoredPhase)
    }

    @Test
    fun `when another target restores first, then the saved state is kept`() {
        val action = TestAction(type = "redirect")
        createStore(target = CheckoutTarget.PaymentMethod("scheme")).save(CheckoutFlowPhase.HandlingAction(action))
        val restoredHandle = savedStateHandle.rebuildFromSavedState()

        createStore(target = CheckoutTarget.PaymentMethod("googlepay"), handle = restoredHandle).restore()

        assertEquals(
            CheckoutFlowPhase.HandlingAction(action),
            createStore(target = CheckoutTarget.PaymentMethod("scheme"), handle = restoredHandle).restore(),
        )
    }

    @Test
    fun `when two targets save a phase, then each restores its own`() {
        val cardTarget = CheckoutTarget.PaymentMethod("scheme")
        val googlePayTarget = CheckoutTarget.PaymentMethod("googlepay")
        createStore(target = cardTarget).save(CheckoutFlowPhase.Submitted)
        createStore(target = googlePayTarget).save(CheckoutFlowPhase.Input)
        val restoredHandle = savedStateHandle.rebuildFromSavedState()

        assertEquals(CheckoutFlowPhase.Submitted, createStore(target = cardTarget, handle = restoredHandle).restore())
        assertEquals(CheckoutFlowPhase.Input, createStore(target = googlePayTarget, handle = restoredHandle).restore())
    }

    @Test
    fun `when another target clears its saved state, then the saved state is kept`() {
        val cardTarget = CheckoutTarget.PaymentMethod("scheme")
        val googlePayTarget = CheckoutTarget.PaymentMethod("googlepay")
        createStore(target = cardTarget).save(CheckoutFlowPhase.Submitted)
        createStore(target = googlePayTarget).save(CheckoutFlowPhase.Submitted)

        createStore(target = googlePayTarget).clear()

        assertEquals(CheckoutFlowPhase.Submitted, createRestoredStore(target = cardTarget).restore())
    }

    @Test
    fun `when a payment method and a stored payment method share the same value, then they are different targets`() {
        createStore(target = CheckoutTarget.PaymentMethod("scheme")).save(CheckoutFlowPhase.Submitted)

        val restoredPhase = createRestoredStore(target = CheckoutTarget.StoredPaymentMethod("scheme")).restore()

        assertNull(restoredPhase)
    }

    @Test
    fun `when the saved state has no target, then a store without target restores it`() {
        createStore(target = null).save(CheckoutFlowPhase.Submitted)

        assertEquals(CheckoutFlowPhase.Submitted, createRestoredStore(target = null).restore())
    }

    @Test
    fun `when the saved state has no target, then a store with a target does not restore it`() {
        createStore(target = null).save(CheckoutFlowPhase.Submitted)

        assertNull(createRestoredStore().restore())
    }

    @Test
    fun `when a flow for the same target restores again on the same handle, then nothing is restored`() {
        createStore().restore()
        createStore().save(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))

        val restoredPhase = createStore().restore()

        assertNull(restoredPhase)
    }

    @Test
    fun `when a flow for the same target restores again on the same handle, then the abandoned phase is cleared`() {
        createStore().restore()
        createStore().save(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))

        createStore().restore()

        assertNull(createRestoredStore().restore())
    }

    @Test
    fun `when a flow restores again on a rebuilt handle, then only the first restore returns the phase`() {
        createStore().save(CheckoutFlowPhase.Submitted)
        val restoredHandle = savedStateHandle.rebuildFromSavedState()

        val firstPhase = createStore(handle = restoredHandle).restore()
        val secondPhase = createStore(handle = restoredHandle).restore()

        assertEquals(CheckoutFlowPhase.Submitted, firstPhase)
        assertNull(secondPhase)
    }

    @Test
    fun `when the process dies again after a restore, then the phase is restored again`() {
        val action = TestAction(type = "await", paymentData = "test_payment_data")
        createStore().save(CheckoutFlowPhase.HandlingAction(action))
        val firstRestoredHandle = savedStateHandle.rebuildFromSavedState()
        val firstRestoredPhase = createStore(handle = firstRestoredHandle).restore()
        val secondRestoredHandle = firstRestoredHandle.rebuildFromSavedState()

        val secondRestoredPhase = createStore(handle = secondRestoredHandle).restore()

        assertEquals(CheckoutFlowPhase.HandlingAction(action), firstRestoredPhase)
        assertEquals(CheckoutFlowPhase.HandlingAction(action), secondRestoredPhase)
    }

    private fun createStore(
        target: CheckoutTarget? = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
        handle: SavedStateHandle = savedStateHandle,
    ) = CheckoutFlowStateStore(
        savedStateHandle = handle,
        target = target,
    )

    /**
     * A store for a flow that is created after process death, on the rebuilt saved state handle.
     */
    private fun createRestoredStore(
        target: CheckoutTarget? = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
    ) = createStore(target = target, handle = savedStateHandle.rebuildFromSavedState())

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "scheme"
    }
}
