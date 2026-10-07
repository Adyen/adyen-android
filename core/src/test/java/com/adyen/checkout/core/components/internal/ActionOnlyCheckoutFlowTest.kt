/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/5/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class ActionOnlyCheckoutFlowTest(
    @param:Mock private val actionHandler: ActionHandler,
) {

    private val stateStore = CheckoutFlowStateStore(SavedStateHandle(), target = null)

    @Test
    fun `when created, then handleAction is called on actionHandler`() {
        val action = createAction()

        createFlow(action)

        verify(actionHandler).handleAction(action)
    }

    @Test
    fun `when paymentComponent is accessed, then null is returned`() {
        val flow = createFlow()

        assertNull(flow.paymentComponent)
    }

    @Test
    fun `when actionComponent is accessed, then actionHandler actionComponent is returned`() {
        val mockActionComponent = mock<ActionComponent>()
        whenever(actionHandler.actionComponent).thenReturn(mockActionComponent)

        val flow = createFlow()

        assertEquals(mockActionComponent, flow.actionComponent)
    }

    @Test
    fun `when submit is called, then no exception is thrown`() {
        val flow = createFlow()

        flow.submit()
    }

    @Nested
    inner class RestoreTest {

        @Test
        fun `when the same action was being handled, then it is restored`() {
            val action = createAction()
            stateStore.save(CheckoutFlowPhase.HandlingAction(action))

            createFlow(action)

            verify(actionHandler).restoreAction(action)
        }

        @Test
        fun `when the same action was being handled, then it is not handled again`() {
            val action = createAction()
            stateStore.save(CheckoutFlowPhase.HandlingAction(action))

            createFlow(action)

            verify(actionHandler, never()).handleAction(any())
        }

        @Test
        fun `when another action was being handled, then the new action is handled`() {
            stateStore.save(CheckoutFlowPhase.HandlingAction(createAction(paymentData = "previous_data")))
            val action = createAction()

            createFlow(action)

            verify(actionHandler).handleAction(action)
            verify(actionHandler, never()).restoreAction(any())
        }
    }

    private fun createFlow(action: TestAction = createAction()) = ActionOnlyCheckoutFlow(
        action = action,
        actionHandler = actionHandler,
        stateStore = stateStore,
    )

    private fun createAction(paymentData: String = "test_data") = TestAction(
        type = "redirect",
        paymentData = paymentData,
        paymentMethodType = "scheme",
    )
}
