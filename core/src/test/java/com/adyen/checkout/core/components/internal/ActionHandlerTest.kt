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
import com.adyen.checkout.core.action.data.ActionData
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.action.internal.RestorableActionComponent
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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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

    private val savedStateHandle = SavedStateHandle()

    private val stateStore = CheckoutFlowStateStore(savedStateHandle, target = null)

    private var receivedSavedStateHandle: SavedStateHandle? = null

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
        fun `when handleAction is called, then the action component receives the saved state handle`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            assertSame(savedStateHandle, receivedSavedStateHandle)
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

        @Test
        fun `when the action type is not registered, then failure is called on the dispatcher`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = "unregistered_actionType"))

            val captor = argumentCaptor<CheckoutError>()
            verify(componentRequestDispatcher).failure(captor.capture())

            val error = captor.firstValue
            assertEquals(CheckoutError.ErrorCode.GENERIC, error.code)
            assertEquals(
                "Action type 'unregistered_actionType' is not supported. " +
                    "Ensure the corresponding module is included in your build dependencies.",
                error.message,
            )
        }

        @Test
        fun `when the action type is not registered, then actionComponent stays null`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = "unregistered_actionType"))

            assertNull(actionHandler.actionComponent)
        }

        @Test
        fun `when the action type is not registered after a successful action, then actionComponent is cleared`() {
            val actionHandler = createActionHandler()
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            actionHandler.handleAction(TestAction(type = "unregistered_actionType"))

            assertNull(actionHandler.actionComponent)
        }
    }

    @Nested
    inner class OnActionTest {

        private val capturedActions = mutableListOf<ActionData>()

        @ParameterizedTest
        @ValueSource(strings = ["redirect", "nativeRedirect", "threeDS2", "sdk", "qrCode", "await", "voucher"])
        fun `when handleAction is called, then onAction is called with the action type`(actionType: String) {
            registerTestFactory(actionType)
            val actionHandler = createActionHandler(onAction = { capturedActions += it })

            actionHandler.handleAction(TestAction(type = actionType))

            assertEquals(listOf(ActionData(actionType)), capturedActions)
        }

        @Test
        fun `when handleAction is called, then onAction is called before the component handles the action`() {
            var handleActionCallCountOnAction: Int? = null
            lateinit var actionHandler: ActionHandler
            actionHandler = createActionHandler(
                onAction = {
                    handleActionCallCountOnAction =
                        (actionHandler.actionComponent as ControllableActionComponent).handleActionCallCount
                },
            )

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            assertEquals(0, handleActionCallCountOnAction)
        }

        @Test
        fun `when handleAction is called twice, then onAction is called twice`() {
            val actionHandler = createActionHandler(onAction = { capturedActions += it })

            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))
            actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

            assertEquals(List(2) { ActionData(TEST_ACTION_TYPE) }, capturedActions)
        }

        @Test
        fun `when the action type is not registered, then onAction is not called`() {
            val actionHandler = createActionHandler(onAction = { capturedActions += it })

            actionHandler.handleAction(TestAction(type = "unregistered_actionType"))

            assertEquals(emptyList<ActionData>(), capturedActions)
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

        @Test
        fun `when ActionDetails event is emitted and result is Completion, then the saved state is cleared`() =
            runTest {
                whenever(componentRequestDispatcher.additionalDetails(any())) doReturn
                    AdditionalDetailsResult.Completion("Authorised")
                val actionHandler = createActionHandler()
                actionHandler.handleAction(TestAction(type = TEST_ACTION_TYPE))

                eventFlow.emit(ActionComponentEvent.ActionDetails(ActionComponentData()))

                assertNull(stateStore.restore())
            }
    }

    @Nested
    inner class RestoreActionTest {

        @BeforeEach
        fun beforeEach() {
            registerRestorableTestFactory()
        }

        @Test
        fun `when restoreAction is called, then actionComponent is set`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            assertNotNull(actionHandler.actionComponent)
        }

        @Test
        fun `when restoreAction is called, then restoreAction is called on the action component`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            val component = actionHandler.actionComponent as? ControllableRestorableActionComponent
            assertEquals(1, component?.restoreActionCallCount)
        }

        @Test
        fun `when restoreAction is called, then handleAction is not called on the action component`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            val component = actionHandler.actionComponent as? ControllableRestorableActionComponent
            assertEquals(0, component?.handleActionCallCount)
        }

        @Test
        fun `when restoreAction is called, then onAction is not called`() {
            val capturedActions = mutableListOf<ActionData>()
            val actionHandler = createActionHandler(onAction = { capturedActions += it })

            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            assertEquals(emptyList<ActionData>(), capturedActions)
        }

        @Test
        fun `when restoreAction is called, then the handling action phase is kept`() {
            val action = TestAction(type = RESTORABLE_ACTION_TYPE)
            stateStore.save(CheckoutFlowPhase.HandlingAction(action))
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(action)

            assertEquals(CheckoutFlowPhase.HandlingAction(action), stateStore.restore())
        }

        @Test
        fun `when the restored component emits ActionDetails, then additional details are dispatched`() = runTest {
            whenever(componentRequestDispatcher.additionalDetails(any())) doReturn
                AdditionalDetailsResult.Completion("Authorised")
            val actionHandler = createActionHandler()
            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            val data = ActionComponentData()
            eventFlow.emit(ActionComponentEvent.ActionDetails(data))

            verify(componentRequestDispatcher).additionalDetails(data)
            verify(componentRequestDispatcher).complete(CheckoutResultCode("Authorised"))
        }

        @Test
        fun `when handleReturn is called after restoreAction, then it reaches the restored component`() {
            val actionHandler = createActionHandler()
            actionHandler.restoreAction(TestAction(type = RESTORABLE_ACTION_TYPE))

            val intent = mock<Intent>()
            actionHandler.handleReturn(intent)

            val component = actionHandler.actionComponent as? ControllableRestorableActionComponent
            assertEquals(intent, component?.lastIntent)
        }

        @Test
        fun `when the action component is not restorable, then a generic failure is reported`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = TEST_ACTION_TYPE))

            with(argumentCaptor<CheckoutError>()) {
                verify(componentRequestDispatcher).failure(capture())
                assertEquals(CheckoutError.ErrorCode.GENERIC, lastValue.code)
            }
        }

        @Test
        fun `when the action component is not restorable, then actionComponent is null`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = TEST_ACTION_TYPE))

            assertNull(actionHandler.actionComponent)
        }

        @Test
        fun `when the action component is not restorable, then the saved state is cleared`() {
            val action = TestAction(type = TEST_ACTION_TYPE)
            stateStore.save(CheckoutFlowPhase.HandlingAction(action))
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(action)

            assertNull(stateStore.restore())
        }

        @Test
        fun `when the action component is not restorable, then handleAction is not called on it`() {
            var handleActionCallCount = 0
            ActionComponentProvider.register(
                TEST_ACTION_TYPE,
                object : ActionFactory<Action, ActionComponent> {
                    override fun create(
                        action: Action,
                        coroutineScope: CoroutineScope,
                        analyticsManager: AnalyticsManager,
                        params: CheckoutParams,
                        savedStateHandle: SavedStateHandle,
                    ) = ControllableActionComponent(eventFlow) { handleActionCallCount++ }
                },
            )
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = TEST_ACTION_TYPE))

            assertEquals(0, handleActionCallCount)
        }

        @Test
        fun `when the action type is not registered, then failure is called on the dispatcher`() {
            val actionHandler = createActionHandler()

            actionHandler.restoreAction(TestAction(type = "unregistered_actionType"))

            verify(componentRequestDispatcher).failure(any())
            assertNull(actionHandler.actionComponent)
        }
    }

    @Nested
    inner class SavedStateTest {

        @Test
        fun `when handleAction is called, then the handling action phase is saved`() {
            val action = TestAction(type = TEST_ACTION_TYPE, paymentData = "test_payment_data")
            val actionHandler = createActionHandler()

            actionHandler.handleAction(action)

            assertEquals(CheckoutFlowPhase.HandlingAction(action), stateStore.restore())
        }

        @Test
        fun `when handleAction is called, then the phase is saved before the component handles the action`() {
            var phaseOnHandleAction: CheckoutFlowPhase? = null
            ActionComponentProvider.register(
                TEST_ACTION_TYPE,
                object : ActionFactory<Action, ActionComponent> {
                    override fun create(
                        action: Action,
                        coroutineScope: CoroutineScope,
                        analyticsManager: AnalyticsManager,
                        params: CheckoutParams,
                        savedStateHandle: SavedStateHandle,
                    ) = ControllableActionComponent(eventFlow) { phaseOnHandleAction = stateStore.restore() }
                },
            )
            val action = TestAction(type = TEST_ACTION_TYPE)
            val actionHandler = createActionHandler()

            actionHandler.handleAction(action)

            assertEquals(CheckoutFlowPhase.HandlingAction(action), phaseOnHandleAction)
        }

        @Test
        fun `when the action type is not registered, then the phase is not saved`() {
            val actionHandler = createActionHandler()

            actionHandler.handleAction(TestAction(type = "unregistered_actionType"))

            assertNull(stateStore.restore())
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
            object : ActionFactory<Action, ActionComponent> {
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

    private fun registerRestorableTestFactory() {
        ActionComponentProvider.register(
            RESTORABLE_ACTION_TYPE,
            object : ActionFactory<Action, ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle,
                ) = ControllableRestorableActionComponent(eventFlow)
            },
        )
    }

    private fun registerTestFactory(actionType: String = TEST_ACTION_TYPE) {
        ActionComponentProvider.register(
            actionType,
            object : ActionFactory<Action, ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle,
                ): ActionComponent {
                    receivedSavedStateHandle = savedStateHandle
                    return ControllableActionComponent(eventFlow)
                }
            },
        )
    }

    private fun createActionHandler(
        onAction: (ActionData) -> Unit = {},
    ) = ActionHandler(
        componentRequestDispatcher = componentRequestDispatcher,
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        analyticsManager = TestAnalyticsManager(),
        params = generateCheckoutParams(),
        savedStateHandle = savedStateHandle,
        stateStore = stateStore,
        onAction = onAction,
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
        private val onHandleAction: () -> Unit = {},
    ) : ActionComponent {

        var handleActionCallCount = 0
            private set

        @Composable
        override fun Content(modifier: Modifier) = Unit

        override fun handleAction() {
            handleActionCallCount++
            onHandleAction()
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

    private class ControllableRestorableActionComponent(
        override val eventFlow: Flow<ActionComponentEvent>,
    ) : ActionComponent, ReturningActionComponent, RestorableActionComponent {

        var handleActionCallCount = 0
            private set

        var restoreActionCallCount = 0
            private set

        var lastIntent: Intent? = null
            private set

        @Composable
        override fun Content(modifier: Modifier) = Unit

        override fun handleAction() {
            handleActionCallCount++
        }

        override fun restoreAction() {
            restoreActionCallCount++
        }

        override fun handleReturn(intent: Intent) {
            lastIntent = intent
        }
    }

    companion object {
        private const val TEST_ACTION_TYPE = "test_action"
        private const val RETURNING_ACTION_TYPE = "returning_action"
        private const val RESTORABLE_ACTION_TYPE = "restorable_action"
    }
}
