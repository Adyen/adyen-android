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
    fun `when a phase is saved, then a new store on the same handle restores it`() {
        val action = TestAction(type = "redirect", paymentData = "test_payment_data")
        createStore().save(CheckoutFlowPhase.HandlingAction(action))

        val restoredPhase = createStore().restore()

        assertEquals(CheckoutFlowPhase.HandlingAction(action), restoredPhase)
    }

    @Test
    fun `when a phase is saved again, then the latest phase is restored`() {
        val store = createStore()
        store.save(CheckoutFlowPhase.Submitted)

        store.save(CheckoutFlowPhase.Input)

        assertEquals(CheckoutFlowPhase.Input, createStore().restore())
    }

    @Test
    fun `when the saved state is cleared, then restore returns null`() {
        val store = createStore()
        store.save(CheckoutFlowPhase.Submitted)

        store.clear()

        assertNull(createStore().restore())
    }

    @Test
    fun `when the saved state belongs to another target, then restore returns null`() {
        createStore(target = CheckoutTarget.PaymentMethod("ideal")).save(CheckoutFlowPhase.Submitted)

        val restoredPhase = createStore(target = CheckoutTarget.PaymentMethod("scheme")).restore()

        assertNull(restoredPhase)
    }

    @Test
    fun `when another target restores first, then the saved state is kept`() {
        val action = TestAction(type = "redirect")
        createStore(target = CheckoutTarget.PaymentMethod("scheme")).save(CheckoutFlowPhase.HandlingAction(action))

        createStore(target = CheckoutTarget.PaymentMethod("googlepay")).restore()

        assertEquals(
            CheckoutFlowPhase.HandlingAction(action),
            createStore(target = CheckoutTarget.PaymentMethod("scheme")).restore(),
        )
    }

    @Test
    fun `when two targets save a phase, then each restores its own`() {
        val cardTarget = CheckoutTarget.PaymentMethod("scheme")
        val googlePayTarget = CheckoutTarget.PaymentMethod("googlepay")
        createStore(target = cardTarget).save(CheckoutFlowPhase.Submitted)
        createStore(target = googlePayTarget).save(CheckoutFlowPhase.Input)

        assertEquals(CheckoutFlowPhase.Submitted, createStore(target = cardTarget).restore())
        assertEquals(CheckoutFlowPhase.Input, createStore(target = googlePayTarget).restore())
    }

    @Test
    fun `when another target clears its saved state, then the saved state is kept`() {
        val cardTarget = CheckoutTarget.PaymentMethod("scheme")
        val googlePayTarget = CheckoutTarget.PaymentMethod("googlepay")
        createStore(target = cardTarget).save(CheckoutFlowPhase.Submitted)
        createStore(target = googlePayTarget).save(CheckoutFlowPhase.Submitted)

        createStore(target = googlePayTarget).clear()

        assertEquals(CheckoutFlowPhase.Submitted, createStore(target = cardTarget).restore())
    }

    @Test
    fun `when a payment method and a stored payment method share the same value, then they are different targets`() {
        createStore(target = CheckoutTarget.PaymentMethod("scheme")).save(CheckoutFlowPhase.Submitted)

        val restoredPhase = createStore(target = CheckoutTarget.StoredPaymentMethod("scheme")).restore()

        assertNull(restoredPhase)
    }

    @Test
    fun `when the saved state has no target, then a store without target restores it`() {
        createStore(target = null).save(CheckoutFlowPhase.Submitted)

        assertEquals(CheckoutFlowPhase.Submitted, createStore(target = null).restore())
    }

    @Test
    fun `when the saved state has no target, then a store with a target does not restore it`() {
        createStore(target = null).save(CheckoutFlowPhase.Submitted)

        assertNull(createStore().restore())
    }

    private fun createStore(
        target: CheckoutTarget? = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
    ) = CheckoutFlowStateStore(
        savedStateHandle = savedStateHandle,
        target = target,
    )

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "scheme"
    }
}
