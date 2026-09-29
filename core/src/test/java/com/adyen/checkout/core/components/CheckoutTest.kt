/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.UnsupportedPaymentMethod
import com.adyen.checkout.core.components.internal.ApplicationContextHolder
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.PaymentMethodAvailabilityCheck
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
internal class CheckoutTest {

    @Before
    fun setUp() {
        ApplicationContextHolder.set(RuntimeEnvironment.getApplication())
        PaymentMethodProvider.clear()
    }

    @After
    fun tearDown() {
        PaymentMethodProvider.clear()
        ApplicationContextHolder.reset()
    }

    @Test
    fun `when the payment method type is in the context and the check passes, then true is returned`() = runTest {
        PaymentMethodProvider.register(
            TEST_PAYMENT_METHOD_TYPE,
            generateFactoryWithAvailabilityCheck(isAvailable = true),
        )

        val result = Checkout.isPaymentMethodAvailable(TEST_PAYMENT_METHOD_TYPE, advancedContext())

        assertTrue(result)
    }

    @Test
    fun `when the payment method type is in the context and the check fails, then false is returned`() = runTest {
        PaymentMethodProvider.register(
            TEST_PAYMENT_METHOD_TYPE,
            generateFactoryWithAvailabilityCheck(isAvailable = false),
        )

        val result = Checkout.isPaymentMethodAvailable(TEST_PAYMENT_METHOD_TYPE, advancedContext())

        assertFalse(result)
    }

    @Test
    fun `when the payment method type is in the context and the check throws, then false is returned`() = runTest {
        PaymentMethodProvider.register(
            TEST_PAYMENT_METHOD_TYPE,
            generateFactoryWithAvailabilityCheck(error = RuntimeException("check failed")),
        )

        val result = Checkout.isPaymentMethodAvailable(TEST_PAYMENT_METHOD_TYPE, advancedContext())

        assertFalse(result)
    }

    @Test
    fun `when the payment method type is not in the context, then false is returned`() = runTest {
        val result = Checkout.isPaymentMethodAvailable("not_a_payment_method", advancedContext())

        assertFalse(result)
    }

    @Test
    fun `when the payment method type is unsupported, then false is returned`() = runTest {
        val context = advancedContext(
            UnsupportedPaymentMethod(type = UNSUPPORTED_PAYMENT_METHOD_TYPE, name = "Unsupported"),
        )
        val result = Checkout.isPaymentMethodAvailable(UNSUPPORTED_PAYMENT_METHOD_TYPE, context)

        assertFalse(result)
    }

    @Test
    fun `when the registered factory has no availability check, then true is returned`() = runTest {
        PaymentMethodProvider.register(TEST_PAYMENT_METHOD_TYPE, generateFactory())

        val result = Checkout.isPaymentMethodAvailable(TEST_PAYMENT_METHOD_TYPE, advancedContext())

        assertTrue(result)
    }

    private fun advancedContext(
        vararg paymentMethods: PaymentMethod = arrayOf(
            GenericPaymentMethod(type = TEST_PAYMENT_METHOD_TYPE, name = "Test"),
        ),
    ) = CheckoutContext.Advanced(
        paymentMethods = PaymentMethods(
            paymentMethods = paymentMethods.toList(),
        ),
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = "",
        publicKey = null,
    )

    private fun generateFactory() = object : PaymentComponentFactory<PaymentComponent> {
        override fun create(
            paymentMethod: PaymentMethod,
            coroutineScope: CoroutineScope,
            analyticsManager: AnalyticsManager,
            sdkDataProvider: SdkDataProvider,
            params: CheckoutParams,
            additionalCallbacks: Set<CheckoutAdditionalCallback>,
        ) = TestPaymentComponent()
    }

    private fun generateFactoryWithAvailabilityCheck(
        isAvailable: Boolean = true,
        error: Throwable? = null,
    ) = object :
        PaymentComponentFactory<PaymentComponent>,
        PaymentMethodAvailabilityCheck {
        override fun create(
            paymentMethod: PaymentMethod,
            coroutineScope: CoroutineScope,
            analyticsManager: AnalyticsManager,
            sdkDataProvider: SdkDataProvider,
            params: CheckoutParams,
            additionalCallbacks: Set<CheckoutAdditionalCallback>,
        ) = TestPaymentComponent()

        override suspend fun isAvailable(paymentMethod: PaymentMethod, params: CheckoutParams): Boolean {
            error?.let { throw it }
            return isAvailable
        }
    }

    private companion object {
        const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
        const val UNSUPPORTED_PAYMENT_METHOD_TYPE = "unsupported_payment_method"
        const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
