/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/5/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.error.CheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class FullCheckoutFlowTest(
    @param:Mock private val componentRequestDispatcher: SubmittableComponentRequestDispatcher,
    @param:Mock private val actionHandler: ActionHandler,
) {

    private val eventFlow = MutableSharedFlow<PaymentComponentEvent>()

    private var savedStateHandle = SavedStateHandle()

    @BeforeEach
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Nested
    inner class RequiresUserInteractionTest {

        @Test
        fun `when action component is null and payment component requires user interaction, then returns true`() =
            runTest {
                val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                assertTrue(flow.requiresUserInteraction())
            }

        @Test
        fun `when action component is not null, then returns false`() = runTest {
            whenever(actionHandler.actionComponent) doReturn mock<ActionComponent>()

            val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            assertFalse(flow.requiresUserInteraction())
        }

        @Test
        fun `when payment component does not require user interaction, then returns false`() = runTest {
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                requiresUserInteraction = false,
            )

            assertFalse(flow.requiresUserInteraction())
        }
    }

    @Nested
    inner class SubmitTest {

        @Test
        fun `when submit is called, then payment component submit is called`() = runTest {
            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()

            assertEquals(1, component.submitCount)
        }

        @Test
        fun `when submit is called twice, then payment component submit is called twice`() = runTest {
            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()
            flow.submit()

            assertEquals(2, component.submitCount)
        }

        @Test
        fun `when submit results in Retry, then loading is set to false`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()

            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()
            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            assertFalse(component.isLoading)
        }

        @Test
        fun `when submit event is received, then loading is set to true`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")
            val component = TestPaymentComponent(eventFlow)
            createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            assertTrue(component.isLoading)
        }

        @Test
        fun `when submit is called, then a submit request is dispatched`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")
            val component = TestPaymentComponent(eventFlow)
            createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            val state = createPaymentComponentState()
            eventFlow.emit(PaymentComponentEvent.Submit(state))

            with(argumentCaptor<PaymentComponentData<*>>()) {
                verify(componentRequestDispatcher, times(1)).submit(capture())
                assertEquals(state.data, lastValue)
            }
        }

        @Test
        fun `when submit results in Retry and submit is called again, then another submit request is dispatched`() =
            runTest {
                whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()

                val component = TestPaymentComponent(eventFlow)
                createFullCheckoutFlow(
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                    component = component,
                )

                val state1 = createPaymentComponentState()
                eventFlow.emit(PaymentComponentEvent.Submit(state1))
                val state2 = createPaymentComponentState(shopperReference = TEST_SHOPPER_REFERENCE)
                eventFlow.emit(PaymentComponentEvent.Submit(state2))

                with(argumentCaptor<PaymentComponentData<*>>()) {
                    verify(componentRequestDispatcher, times(2)).submit(capture())
                    assertEquals(listOf(state1.data, state2.data), allValues)
                }
            }

        @Test
        fun `when submit results in Completion and submit is called again, then only one submit request is dispatched`() =
            runTest {
                whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")

                val component = TestPaymentComponent(eventFlow)
                createFullCheckoutFlow(
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                    component = component,
                )

                val state1 = createPaymentComponentState()
                eventFlow.emit(PaymentComponentEvent.Submit(state1))
                val state2 = createPaymentComponentState(shopperReference = TEST_SHOPPER_REFERENCE)
                eventFlow.emit(PaymentComponentEvent.Submit(state2))

                with(argumentCaptor<PaymentComponentData<*>>()) {
                    verify(componentRequestDispatcher, times(1)).submit(capture())
                    assertEquals(state1.data, lastValue)
                }
            }

        @Test
        fun `when submit results in Action and submit is called again, then only one submit request is dispatched`() =
            runTest {
                val action = TestAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
                whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Action(action)

                val component = TestPaymentComponent(eventFlow)
                createFullCheckoutFlow(
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                    component = component,
                )

                val state1 = createPaymentComponentState()
                eventFlow.emit(PaymentComponentEvent.Submit(state1))
                val state2 = createPaymentComponentState(shopperReference = TEST_SHOPPER_REFERENCE)
                eventFlow.emit(PaymentComponentEvent.Submit(state2))

                with(argumentCaptor<PaymentComponentData<*>>()) {
                    verify(componentRequestDispatcher, times(1)).submit(capture())
                    assertEquals(state1.data, lastValue)
                }
            }
    }

    @Nested
    inner class HandleResultTest {

        @Test
        fun `when submit results in Action, then actionHandler handleAction is called`() = runTest {
            val action = TestAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Action(action)

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verify(actionHandler).handleAction(action)
        }

        @Test
        fun `when submit results in Completion, then componentRequestDispatcher complete is called`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verify(componentRequestDispatcher).complete(CheckoutResultCode("Authorised"))
        }

        @Test
        fun `when submit results in Retry, then no further interactions occur`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verifyNoInteractions(actionHandler)
        }

        @Test
        fun `when submit results in PartialPayment, then no further interactions occur`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.PartialPayment(
                order = mock(),
                paymentMethods = mock(),
            )

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verifyNoInteractions(actionHandler)
        }
    }

    @Nested
    inner class SavedStateTest {

        @Test
        fun `when submit event is received, then the submitted phase is saved before the request is dispatched`() =
            runTest {
                var phaseDuringSubmit: CheckoutFlowPhase? = null
                whenever(componentRequestDispatcher.submit(any())) doSuspendableAnswer {
                    phaseDuringSubmit = savedPhase()
                    SubmitResult.Retry()
                }
                createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

                assertEquals(CheckoutFlowPhase.Submitted, phaseDuringSubmit)
            }

        @Test
        fun `when submit results in Retry, then the input phase is saved`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()
            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            assertEquals(CheckoutFlowPhase.Input, savedPhase())
        }

        @Test
        fun `when submit results in Completion, then the saved state is cleared`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")
            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            assertNull(savedPhase())
        }
    }

    @Nested
    inner class RestoreTest {

        @Test
        fun `when restored in the submitted phase, then a generic failure is reported`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.Submitted)

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            with(argumentCaptor<CheckoutError>()) {
                verify(componentRequestDispatcher).failure(capture())
                assertEquals(CheckoutError.ErrorCode.GENERIC, lastValue.code)
            }
        }

        @Test
        fun `when restored in the submitted phase, then submit requests are not dispatched`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.Submitted)
            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verify(componentRequestDispatcher, never()).submit(any())
        }

        @Test
        fun `when restored in the submitted phase, then the saved state is cleared`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.Submitted)

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            assertNull(savedPhase())
        }

        @Test
        fun `when restored in the handling action phase, then the action is restored`() = runTest {
            val action = TestAction(type = "redirect", paymentData = "test_data")
            givenSavedPhase(CheckoutFlowPhase.HandlingAction(action))

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            verify(actionHandler).restoreAction(action)
        }

        @Test
        fun `when restored in the handling action phase, then submit requests are not dispatched`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))
            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verify(componentRequestDispatcher, never()).submit(any())
        }

        @Test
        fun `when restored in the handling action phase, then the flow reports no failure`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            verify(componentRequestDispatcher, never()).failure(any())
        }

        @Test
        fun `when a flow for the same target is created again on the same handle, then the action is not restored`() =
            runTest {
                createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))
                createStateStore().save(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))

                createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                verify(actionHandler, never()).restoreAction(any())
            }

        @Test
        fun `when a flow for the same target is created again on the same handle, then submit requests are dispatched`() =
            runTest {
                whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()
                val firstFlowScope = CoroutineScope(UnconfinedTestDispatcher())
                createFullCheckoutFlow(firstFlowScope)
                createStateStore().save(CheckoutFlowPhase.HandlingAction(TestAction(type = "redirect")))
                firstFlowScope.cancel()
                createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

                verify(componentRequestDispatcher).submit(any())
            }

        @Test
        fun `when restored in the input phase, then no action is restored`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.Input)

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            verify(actionHandler, never()).restoreAction(any())
        }

        @Test
        fun `when restored in the input phase, then no failure is reported`() = runTest {
            givenSavedPhase(CheckoutFlowPhase.Input)

            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            verify(componentRequestDispatcher, never()).failure(any())
        }

        @Test
        fun `when restored in the input phase, then submit requests are dispatched`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()
            givenSavedPhase(CheckoutFlowPhase.Input)
            createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            verify(componentRequestDispatcher).submit(any())
        }
    }

    private fun createStateStore(handle: SavedStateHandle = savedStateHandle) = CheckoutFlowStateStore(
        savedStateHandle = handle,
        target = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
    )

    /**
     * Saves [phase] as a flow before process death would, and continues with the rebuilt saved state handle.
     */
    private fun givenSavedPhase(phase: CheckoutFlowPhase) {
        createStateStore().save(phase)
        savedStateHandle = savedStateHandle.rebuildFromSavedState()
    }

    /**
     * The phase that a flow created after process death would restore.
     */
    private fun savedPhase() = createStateStore(handle = savedStateHandle.rebuildFromSavedState()).restore()

    private fun createFullCheckoutFlow(
        coroutineScope: CoroutineScope,
        requiresUserInteraction: Boolean = true,
        component: PaymentComponent = TestPaymentComponent(eventFlow, requiresUserInteraction),
    ): FullCheckoutFlow {
        registerComponent(component)

        return FullCheckoutFlow(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            paymentComponent = component,
            actionHandler = actionHandler,
            stateStore = createStateStore(),
        )
    }

    private fun registerComponent(component: PaymentComponent) {
        PaymentMethodProvider.register(
            TEST_PAYMENT_METHOD_TYPE,
            object : PaymentComponentFactory<PaymentComponent> {
                override fun create(
                    paymentMethod: com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    sdkDataProvider: SdkDataProvider,
                    params: CheckoutParams,
                    additionalCallbacks: Set<CheckoutAdditionalCallback>,
                ) = component
            },
        )
    }

    private fun createPaymentComponentState(
        shopperReference: String? = null
    ): PaymentComponentState<PaymentMethodDetails> {
        return object : PaymentComponentState<PaymentMethodDetails> {
            override val data = PaymentComponentData<PaymentMethodDetails>(
                paymentMethod = null,
                order = null,
                shopperReference = shopperReference,
            )
            override val isValid = true
        }
    }

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
        private const val TEST_SHOPPER_REFERENCE = "test_shopper_reference"
    }
}
