/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/8/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.action.internal.TestActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.ActionOnlyCheckoutCallbacks
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InvalidConfigurationError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
internal class CheckoutControllerFactoryTest {

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setUp() {
        ApplicationContextHolder.set(RuntimeEnvironment.getApplication())
        PaymentMethodProvider.clear()
        ActionComponentProvider.clear()
    }

    @After
    fun tearDown() {
        PaymentMethodProvider.clear()
        ActionComponentProvider.clear()
        ApplicationContextHolder.reset()
    }

    @Test
    fun `when an action only controller is created, then its action component receives the saved state handle`() {
        var receivedSavedStateHandle: SavedStateHandle? = null
        ActionComponentProvider.register(
            TEST_ACTION_TYPE,
            object : ActionFactory<Action, ActionComponent> {
                override fun create(
                    action: Action,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    savedStateHandle: SavedStateHandle,
                ): ActionComponent {
                    receivedSavedStateHandle = savedStateHandle
                    return TestActionComponent()
                }
            },
        )

        createActionOnlyController()

        assertSame(savedStateHandle, receivedSavedStateHandle)
    }

    @Test
    fun `when creation throws InvalidConfigurationError, then CheckoutException with INVALID_CONFIGURATION is thrown`() {
        registerThrowingFactory(InvalidConfigurationError("missing merchant account"))

        val exception = assertThrows(CheckoutException::class.java) {
            createAdvancedController()
        }

        assertEquals(CheckoutError.ErrorCode.INVALID_CONFIGURATION, exception.error.code)
        assertEquals("missing merchant account", exception.error.message)
    }

    @Test
    fun `when creation throws GenericError, then CheckoutException with GENERIC is thrown`() {
        registerThrowingFactory(GenericError("something went wrong"))

        val exception = assertThrows(CheckoutException::class.java) {
            createAdvancedController()
        }

        assertEquals(CheckoutError.ErrorCode.GENERIC, exception.error.code)
        assertEquals("something went wrong", exception.error.message)
    }

    @Test
    fun `when creation throws a non internal error, then it is not converted`() {
        registerThrowingFactory(IllegalArgumentException("Incorrect paymentMethod"))

        assertThrows(IllegalArgumentException::class.java) {
            createAdvancedController()
        }
    }

    private fun createAdvancedController() = CheckoutControllerFactory().create(
        target = CheckoutTarget.PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
        context = CheckoutContext.Advanced(
            paymentMethods = PaymentMethods(
                paymentMethods = listOf(
                    GenericPaymentMethod(type = TEST_PAYMENT_METHOD_TYPE, name = "Test"),
                ),
            ),
            checkoutConfiguration = CheckoutConfiguration(
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY,
                shopperLocale = Locale.US,
            ),
            checkoutAttemptId = "",
            publicKey = null,
        ),
        callbacks = AdvancedCheckoutCallbacks(
            onSubmit = { SubmitResult.Completion("Authorised") },
            onAction = {},
            onAdditionalDetails = { AdditionalDetailsResult.Completion("Authorised") },
            onFailure = {},
        ),
        coroutineScope = coroutineScope,
        savedStateHandle = savedStateHandle,
    )

    private fun createActionOnlyController() = CheckoutControllerFactory().create(
        context = CheckoutContext.ActionOnly(
            action = TestAction(type = TEST_ACTION_TYPE),
            checkoutConfiguration = CheckoutConfiguration(
                environment = Environment.TEST,
                clientKey = TEST_CLIENT_KEY,
                shopperLocale = Locale.US,
            ),
            checkoutAttemptId = "",
            publicKey = null,
        ),
        callbacks = ActionOnlyCheckoutCallbacks(
            onAdditionalDetails = { AdditionalDetailsResult.Completion("Authorised") },
            onFailure = {},
        ),
        coroutineScope = coroutineScope,
        savedStateHandle = savedStateHandle,
    )

    private fun registerThrowingFactory(throwable: Throwable) {
        PaymentMethodProvider.register(
            TEST_PAYMENT_METHOD_TYPE,
            object : PaymentComponentFactory<PaymentComponent> {
                override fun create(
                    paymentMethod: PaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    sdkDataProvider: SdkDataProvider,
                    params: CheckoutParams,
                    additionalCallbacks: Set<CheckoutAdditionalCallback>,
                ): PaymentComponent = throw throwable
            },
        )
    }

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
        private const val TEST_ACTION_TYPE = "test_action"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
