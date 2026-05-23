/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/5/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.IntegrationType
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutPaymentMethodRoute
import com.adyen.checkout.core.components.CheckoutSecondaryRoute
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.InstantPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class FullCheckoutFlowTest {

    private val eventFlow = MutableSharedFlow<PaymentComponentEvent>()

    @Mock
    private lateinit var componentRequestDispatcher: ComponentRequestDispatcher

    @Mock
    private lateinit var actionHandler: ActionHandler

    @BeforeEach
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Nested
    inner class NavigationTest {

        @Test
        fun `when SecondaryScreen event is emitted, then paymentMethodNavigation emits Secondary route`() = runTest {
            val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            val paymentMethodNavigation = flow.paymentMethodNavigation.test(testScheduler)
            eventFlow.emit(PaymentComponentEvent.SecondaryScreen("test_id"))

            val expected = paymentMethodNavigation.latestValue
            assertInstanceOf<CheckoutPaymentMethodRoute.Secondary>(expected)
            assertEquals("test_id", expected.identifier)
        }

        @Test
        fun `when CloseSecondaryScreen event is emitted, then secondaryNavigation emits PaymentMethod route`() =
            runTest {
                val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                val secondaryNavigation = flow.secondaryNavigation.test(testScheduler)
                eventFlow.emit(PaymentComponentEvent.CloseSecondaryScreen)

                val expected = secondaryNavigation.latestValue
                assertInstanceOf<CheckoutSecondaryRoute.PaymentMethod>(expected)
            }

        @Test
        fun `when submit results in Action, then paymentMethodNavigation emits Action route`() = runTest {
            val action = RedirectAction(type = "redirect", paymentData = "test_data", paymentMethodType = "scheme")
            whenever(componentRequestDispatcher.submit(any())) doReturn SubmitResult.Action(action)

            val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

            val paymentMethodNavigation = flow.paymentMethodNavigation.test(testScheduler)
            eventFlow.emit(PaymentComponentEvent.Submit(createPaymentComponentState()))

            val expected = paymentMethodNavigation.latestValue
            assertInstanceOf<CheckoutPaymentMethodRoute.Action>(expected)
        }

        @Test
        fun `when multiple SecondaryScreen events are emitted, then paymentMethodNavigation emits corresponding routes`() =
            runTest {
                val flow = createFullCheckoutFlow(CoroutineScope(UnconfinedTestDispatcher()))

                val paymentMethodNavigation = flow.paymentMethodNavigation.test(testScheduler)
                eventFlow.emit(PaymentComponentEvent.SecondaryScreen("first"))
                eventFlow.emit(PaymentComponentEvent.SecondaryScreen("second"))

                val expected = listOf(
                    CheckoutPaymentMethodRoute.Secondary("first"),
                    CheckoutPaymentMethodRoute.Secondary("second"),
                )
                assertEquals(expected, paymentMethodNavigation.values)
            }
    }

    private fun createFullCheckoutFlow(coroutineScope: CoroutineScope): FullCheckoutFlow {
        val testComponent = TestPaymentComponent(eventFlow)
        registerComponent(testComponent)

        return FullCheckoutFlow(
            target = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
            context = createCheckoutContext(),
            callbacks = TestCheckoutCallbacks(),
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = TestAnalyticsManager(),
            params = generateCheckoutParams(),
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
                    params: CheckoutParams,
                    additionalCallbacks: Set<CheckoutAdditionalCallback>,
                ) = component
            },
        )
    }

    private fun createCheckoutContext(): CheckoutContext.Advanced {
        return CheckoutContext.Advanced(
            paymentMethods = PaymentMethods(
                paymentMethods = listOf(
                    InstantPaymentMethod(type = TEST_PAYMENT_METHOD_TYPE, name = "Test"),
                ),
            ),
            checkoutConfiguration = createCheckoutConfiguration(),
            checkoutAttemptId = null,
            publicKey = null,
        )
    }

    private fun createCheckoutConfiguration() = CheckoutConfiguration(
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfgh",
        shopperLocale = Locale.US,
    )

    private fun generateCheckoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfgh",
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = "test_publicKey",
        integrationType = IntegrationType.COMPONENTS,
        additionalConfigurations = emptyMap(),
        additionalSessionParams = null,
    )

    private fun createPaymentComponentState(): PaymentComponentState<PaymentMethodDetails> {
        return object : PaymentComponentState<PaymentMethodDetails> {
            override val data = PaymentComponentData<PaymentMethodDetails>(paymentMethod = null, order = null)
            override val isValid = true
        }
    }

    private class TestCheckoutCallbacks : CheckoutCallbacks(additionalCallbacksBlock = {})

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
    }
}
