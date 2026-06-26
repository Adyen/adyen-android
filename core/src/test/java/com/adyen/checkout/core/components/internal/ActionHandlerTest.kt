/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/5/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.action.internal.ReturningActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.internal.GenericError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class ActionHandlerTest(
    @param:Mock private val componentRequestDispatcher: ComponentRequestDispatcher,
) {

    private val eventFlow = MutableSharedFlow<ActionComponentEvent>()

    @BeforeEach
    fun beforeEach() {
        ActionComponentProvider.clear()
        registerTestFactory()
    }

    @AfterEach
    fun tearDown() {
        ActionComponentProvider.clear()
    }

    @Nested
    inner class HandleActionTest {

        @Test
        fun `when handleAction is not called, then actionComponent is null`() {
            val actionHandler = createActionHandler()

            assertNull(actionHandler.actionComponent)
        }

        @Test
        fun `when handleAction is called, then actionComponent is set`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            assertNotNull(actionHandler.actionComponent)
        }

        @Test
        fun `when handleAction is called, then handleAction is called on the action component`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            val component = actionHandler.actionComponent as? ControllableActionComponent
            assertEquals(1, component?.handleActionCallCount)
        }

        @Test
        fun `when handleAction is called again, then actionComponent is replaced`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))
            val firstComponent = actionHandler.actionComponent

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))
            val secondComponent = actionHandler.actionComponent

            assertNotSame(firstComponent, secondComponent)
        }

        @Test
        fun `when handleAction is called again, then handleAction is called on the new action component`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            val component = actionHandler.actionComponent as? ControllableActionComponent
            assertEquals(1, component?.handleActionCallCount)
        }
    }

    @Nested
    inner class ActionDetailsEventTest {

        @Test
        fun `when ActionDetails event is emitted and result is Completion, then onComplete is called`() = runTest {
            whenever(componentRequestDispatcher.additionalDetails(any())) doReturn
                AdditionalDetailsResult.Completion("Authorised")

            val actionHandler = createActionHandler()
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            eventFlow.emit(ActionComponentEvent.ActionDetails(ActionComponentData()))

            verify(componentRequestDispatcher).complete(CheckoutResultCode("Authorised"))
        }
    }

    @Nested
    inner class ErrorEventTest {

        @Test
        fun `when Error event is emitted, then error is called on the dispatcher`() = runTest {
            val actionHandler = createActionHandler()
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            val internalError = GenericError("test error")
            eventFlow.emit(ActionComponentEvent.Error(internalError))

            verify(componentRequestDispatcher).failure(
                CheckoutError(
                    code = CheckoutError.ErrorCode.GENERIC,
                    message = "test error",
                    cause = internalError,
                ),
            )
        }
    }

    @Nested
    inner class HandleReturnTest {

        @Test
        fun `when action component is null, then nothing happens`() {
            val actionHandler = createActionHandler()

            val intent = mock<Intent>()
            actionHandler.handleReturn(intent)

            assertNull(actionHandler.actionComponent)
        }

        @Test
        fun `when action component can handle return, then handleReturn is called on the action component`() {
            registerReturningTestFactory()
            val actionHandler = createActionHandler()
            actionHandler.handleAction(TestAction(type = RETURNING_ACTION_TYPE))

            val intent = mock<Intent>()
            actionHandler.handleReturn(intent)

            val component = actionHandler.actionComponent as? ControllableReturningActionComponent
            assertEquals(1, component?.handleReturnCallCount)
            assertEquals(intent, component?.lastIntent)
        }

        @Test
        fun `when action component cannot handle return, then nothing happens`() {
            val actionHandler = createActionHandler()
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            val intent = mock<Intent>()
            actionHandler.handleReturn(intent)

            assert(actionHandler.actionComponent !is ReturningActionComponent)
        }
    }

    private fun registerReturningTestFactory() {
        ActionComponentProvider.register(
            RETURNING_ACTION_TYPE,
            object : ActionFactory<ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle,
                ) = ControllableReturningActionComponent(eventFlow)
            },
        )
    }

    private fun registerTestFactory() {
        ActionComponentProvider.register(
            TEST_ACTION_TYPE,
            object : ActionFactory<ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle,
                ) = ControllableActionComponent(eventFlow)
            },
        )
    }

    private fun createActionHandler() = ActionHandler(
        componentRequestDispatcher = componentRequestDispatcher,
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        analyticsManager = TestAnalyticsManager(),
        params = generateCheckoutParams(),
    )

    private fun generateCheckoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfgh",
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = "test_publicKey",
        additionalConfigurations = emptyMap(),
        additionalSessionParams = null,
    )

    private class ControllableActionComponent(
        override val eventFlow: Flow<ActionComponentEvent>,
    ) : ActionComponent {

        var handleActionCallCount = 0
            private set

        @Composable
        override fun Content(modifier: Modifier) = Unit

        override fun handleAction() {
            handleActionCallCount++
        }
    }

    private class ControllableReturningActionComponent(
        override val eventFlow: Flow<ActionComponentEvent>,
    ) : ActionComponent, ReturningActionComponent {

        var handleActionCallCount = 0
            private set

        var handleReturnCallCount = 0
            private set

        var lastIntent: Intent? = null
            private set

        @Composable
        override fun Content(modifier: Modifier) = Unit

        override fun handleAction() {
            handleActionCallCount++
        }

        override fun handleReturn(intent: Intent) {
            handleReturnCallCount++
            lastIntent = intent
        }
    }

    companion object {
        private const val TEST_ACTION_TYPE = "test_action"
        private const val RETURNING_ACTION_TYPE = "returning_action"
    }
}
