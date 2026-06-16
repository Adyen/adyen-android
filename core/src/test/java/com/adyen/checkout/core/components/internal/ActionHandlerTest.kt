/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/5/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.internal.GenericError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class ActionHandlerTest(
    @param:Mock private val componentRequestDispatcher: ComponentRequestDispatcher,
    @param:Mock private val analyticsManager: AnalyticsManager,
) {

    private val eventFlow = MutableSharedFlow<ActionComponentEvent>()

    @BeforeEach
    fun setUp() {
        ActionComponentProvider.clear()
    }

    @Nested
    inner class HandleActionTest {

        @Test
        fun `when handleAction is called, then actionComponent is set`() = runTest {
            val mockActionComponent = registerTestActionComponent()
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            actionHandler.handleAction(createAction())

            assertEquals(mockActionComponent, actionHandler.actionComponent)
        }

        @Test
        fun `when handleAction is called, then handleAction is called on action component`() = runTest {
            val mockActionComponent = registerTestActionComponent()
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            actionHandler.handleAction(createAction())

            verify(mockActionComponent).handleAction()
        }

        @Test
        fun `when handleAction is called twice, then actionComponent is updated`() = runTest {
            val secondEventFlow = MutableSharedFlow<ActionComponentEvent>()
            val firstComponent = registerTestActionComponent()
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            actionHandler.handleAction(createAction())
            assertEquals(firstComponent, actionHandler.actionComponent)

            val secondComponent = mock<ActionComponent>()
            whenever(secondComponent.eventFlow).thenReturn(secondEventFlow)
            registerActionComponent(secondComponent)

            actionHandler.handleAction(createAction())
            assertEquals(secondComponent, actionHandler.actionComponent)
        }
    }

    @Nested
    inner class EventHandlingTest {

        @Test
        fun `when ActionDetails event is emitted, then additionalDetails is called on dispatcher`() = runTest {
            registerTestActionComponent()
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            actionHandler.handleAction(createAction())

            val data = ActionComponentData(paymentData = "test_data")
            eventFlow.emit(ActionComponentEvent.ActionDetails(data))

            verify(componentRequestDispatcher).additionalDetails(data)
        }

        @Test
        fun `when Error event is emitted, then error is called on dispatcher`() = runTest {
            registerTestActionComponent()
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            actionHandler.handleAction(createAction())

            val internalError = GenericError("test error message")
            eventFlow.emit(ActionComponentEvent.Error(internalError))

            val captor = argumentCaptor<CheckoutError>()
            verify(componentRequestDispatcher).error(captor.capture())
            assertEquals("test error message", captor.firstValue.message)
        }
    }

    @Nested
    inner class InitialStateTest {

        @Test
        fun `when created, then actionComponent is null`() = runTest {
            val actionHandler = createActionHandler(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

            assertNull(actionHandler.actionComponent)
        }
    }

    private fun createActionHandler(coroutineScope: CoroutineScope): ActionHandler {
        return ActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = CheckoutParams(
                shopperLocale = Locale.US,
                environment = Environment.TEST,
                clientKey = "test_client_key",
                analyticsParams = AnalyticsParams(level = AnalyticsParamsLevel.ALL),
                amount = null,
                showSubmitButton = false,
                publicKey = null,
                additionalConfigurations = emptyMap(),
                additionalSessionParams = null,
            ),
        )
    }

    private fun registerTestActionComponent(): ActionComponent {
        val mockActionComponent = mock<ActionComponent>()
        whenever(mockActionComponent.eventFlow).thenReturn(eventFlow)
        registerActionComponent(mockActionComponent)
        return mockActionComponent
    }

    private fun registerActionComponent(component: ActionComponent) {
        ActionComponentProvider.register(
            ACTION_TYPE,
            object : ActionFactory<ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle
                ): ActionComponent = component
            },
        )
    }

    private fun createAction() = RedirectAction(
        type = ACTION_TYPE,
        paymentData = "test_data",
        paymentMethodType = "scheme",
    )

    companion object {
        private const val ACTION_TYPE = "redirect"
    }
}
