/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/5/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
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

    @BeforeEach
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Nested
    inner class NavigationTest {

        @Test
        fun `when SecondaryScreen event is emitted, then navigation emits Secondary route`() = runTest {
            val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            val navigation = flow.navigation.test(testScheduler)
            eventFlow.emit(PaymentComponentEvent.SecondaryScreen("test_id"))

            val expected = navigation.latestValue
            assertInstanceOf<CheckoutRoute.Secondary>(expected)
            assertEquals("test_id", expected.identifier)
        }

        @Test
        fun `when CloseSecondaryScreen event is emitted, then navigation emits PaymentMethod route`() =
            runTest {
                val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                val navigation = flow.navigation.test(testScheduler)
                eventFlow.emit(PaymentComponentEvent.CloseSecondaryScreen)

                val expected = navigation.latestValue
                assertInstanceOf<CheckoutRoute.PaymentMethod>(expected)
            }

        @Test
        fun `when submit results in Action, then navigation emits Action route`() = runTest {
            val action = RedirectAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Action(action)

            val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            val navigation = flow.navigation.test(testScheduler)
            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            val expected = navigation.latestValue
            assertInstanceOf<CheckoutRoute.Action>(expected)
        }

        @Test
        fun `when multiple SecondaryScreen events are emitted, then navigation emits corresponding routes`() =
            runTest {
                val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                val navigation = flow.navigation.test(testScheduler)
                eventFlow.emit(PaymentComponentEvent.SecondaryScreen("first"))
                eventFlow.emit(PaymentComponentEvent.SecondaryScreen("second"))

                val expected = listOf(
                    CheckoutRoute.Secondary("first"),
                    CheckoutRoute.Secondary("second"),
                )
                assertEquals(expected, navigation.values)
            }
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
        fun `when submit is called, then loading is set to true`() = runTest {
            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()

            assertTrue(component.isLoading)
        }

        @Test
        fun `when submit is called twice, then payment component submit is called only once`() = runTest {
            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()
            flow.submit()

            assertEquals(1, component.submitCount)
        }

        @Test
        fun `when submit results in Retry and submit is called again, then payment component submit is called twice`() =
            runTest {
                whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Retry()

                val component = TestPaymentComponent(eventFlow)
                val flow = createFullCheckoutFlow(
                    coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                    component = component,
                )

                flow.submit()
                eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

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
        fun `when submit results in Completion, then submit cannot be called again`() = runTest {
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Completion("Authorised")

            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()
            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            flow.submit()

            assertEquals(1, component.submitCount)
        }

        @Test
        fun `when submit results in Action, then submit cannot be called again`() = runTest {
            val action = RedirectAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Action(action)

            val component = TestPaymentComponent(eventFlow)
            val flow = createFullCheckoutFlow(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                component = component,
            )

            flow.submit()
            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            flow.submit()

            assertEquals(1, component.submitCount)
        }
    }

    @Nested
    inner class HandleResultTest {

        @Test
        fun `when submit results in Action, then actionHandler handleAction is called`() = runTest {
            val action = RedirectAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
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

    private fun createPaymentComponentState(): PaymentComponentState<PaymentMethodDetails> {
        return object : PaymentComponentState<PaymentMethodDetails> {
            override val data = PaymentComponentData<PaymentMethodDetails>(paymentMethod = null, order = null)
            override val isValid = true
        }
    }

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
    }
}
