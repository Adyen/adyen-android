/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/5/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class ActionOnlyCheckoutFlowTest(
    @param:Mock private val actionHandler: ActionHandler,
) {

    @Test
    fun `when created, then handleAction is called on actionHandler`() {
        val action = createAction()

        ActionOnlyCheckoutFlow(action, actionHandler)

        verify(actionHandler).handleAction(action)
    }

    @Test
    fun `when paymentComponent is accessed, then null is returned`() {
        val flow = ActionOnlyCheckoutFlow(createAction(), actionHandler)

        assertNull(flow.paymentComponent)
    }

    @Test
    fun `when actionComponent is accessed, then actionHandler actionComponent is returned`() {
        val mockActionComponent = mock<ActionComponent>()
        whenever(actionHandler.actionComponent).thenReturn(mockActionComponent)

        val flow = ActionOnlyCheckoutFlow(createAction(), actionHandler)

        assertEquals(mockActionComponent, flow.actionComponent)
    }

    @Test
    fun `when paymentMethodNavigation is collected, then no values are emitted`() = runTest {
        val flow = ActionOnlyCheckoutFlow(createAction(), actionHandler)

        val paymentMethodNavigation = flow.paymentMethodNavigation.test(testScheduler)

        assertTrue(paymentMethodNavigation.values.isEmpty())
    }

    @Test
    fun `when secondaryNavigation is collected, then no values are emitted`() = runTest {
        val flow = ActionOnlyCheckoutFlow(createAction(), actionHandler)

        val secondaryNavigation = flow.secondaryNavigation.test(testScheduler)

        assertTrue(secondaryNavigation.values.isEmpty())
    }

    @Test
    fun `when submit is called, then no exception is thrown`() {
        val flow = ActionOnlyCheckoutFlow(createAction(), actionHandler)

        flow.submit()
    }

    private fun createAction() = RedirectAction(
        type = "redirect",
        paymentData = "test_data",
        paymentMethodType = "scheme",
    )
}
