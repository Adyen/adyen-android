/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/7/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class PaymentMethodProviderTest {

    private val component = TestPaymentComponent()
    private val factory = generateFactory(
        paymentComponent = component,
    )

    @Before
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Test
    fun `when register is called concurrently from multiple threads, then provider should not lose any data`() {
        val threadCount = 10
        val registrationsPerThread = 100
        val totalRegistrations = threadCount * registrationsPerThread
        val executor = Executors.newFixedThreadPool(threadCount)

        // Submit all registration tasks to the thread pool
        for (i in 0 until totalRegistrations) {
            executor.submit {
                PaymentMethodProvider.register("test_method_$i", factory)
            }
        }

        // Wait for all tasks to complete
        executor.shutdown()
        // Giving it a generous timeout to finish all tasks
        val completed = executor.awaitTermination(5, TimeUnit.SECONDS)
        assertTrue("Executor tasks did not complete in time.", completed)
        assertEquals(totalRegistrations, PaymentMethodProvider.getFactoriesCount())
    }

    @Test
    fun `when register is called with an existing txVariant, then the factory is overwritten`() =
        runTest {
            val secondaryComponent = TestPaymentComponent()
            val secondaryFactory = generateFactory(
                paymentComponent = secondaryComponent,
            )

            PaymentMethodProvider.register("txVariant", factory)
            PaymentMethodProvider.register("txVariant", secondaryFactory)

            val actualComponent = PaymentMethodProvider.get(
                txVariant = "txVariant",
                coroutineScope = this,
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentSessionParams = null,
            )
            assertEquals(1, PaymentMethodProvider.getFactoriesCount())
            assertEquals(secondaryComponent, actualComponent)
            assertNotSame(component, actualComponent)
        }

    @Test
    fun `when register is called for different txVariants, then all factories are stored`() {
        PaymentMethodProvider.register("txVariant_one", factory)
        PaymentMethodProvider.register("txVariant_two", factory)

        assertEquals(2, PaymentMethodProvider.getFactoriesCount())
    }

    @Test
    fun `when get is called for a registered factory, then the correct component is returned`() =
        runTest {
            PaymentMethodProvider.register("txVariant", factory)

            val actualComponent = PaymentMethodProvider.get(
                txVariant = "txVariant",
                coroutineScope = this,
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentSessionParams = null,
            )
            assertEquals(1, PaymentMethodProvider.getFactoriesCount())
            Assert.assertSame(component, actualComponent)
        }

    @Test
    fun `when get is called for an unregistered factory, then an error is thrown`() = runTest {
        assertThrows<IllegalStateException> {
            PaymentMethodProvider.get(
                txVariant = "unregistered_txVariant",
                coroutineScope = this,
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentSessionParams = null,
            )
        }
    }

    @Test
    fun `when clear is called, then all factories are removed`() {
        PaymentMethodProvider.register("txVariant_one", factory)
        PaymentMethodProvider.register("txVariant_two", factory)
        assertEquals(2, PaymentMethodProvider.getFactoriesCount())

        PaymentMethodProvider.clear()

        assertEquals(0, PaymentMethodProvider.getFactoriesCount())
    }

    private fun generateFactory(paymentComponent: PaymentComponent<BasePaymentComponentState>) =
        object :
            PaymentMethodFactory<BasePaymentComponentState, PaymentComponent<BasePaymentComponentState>> {
            override fun create(
                coroutineScope: CoroutineScope,
                checkoutConfiguration: CheckoutConfiguration,
                componentSessionParams: SessionParams?
            ) = paymentComponent
        }

    private fun generateCheckoutConfiguration() = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_key_12345",
    )
}
