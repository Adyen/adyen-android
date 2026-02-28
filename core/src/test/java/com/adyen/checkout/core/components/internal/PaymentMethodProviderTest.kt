/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/7/2025.
 */

package com.adyen.checkout.core.components.internal

import android.app.Application
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.components.internal.ui.model.generateComponentParamsBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
internal class PaymentMethodProviderTest {

    private val component = TestPaymentComponent()
    private val factory = generateFactory(
        paymentComponent = component,
    )

    private val storedFactory = generateStoredFactory(
        paymentComponent = component,
    )

    @BeforeEach
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
        assertTrue(completed, "Executor tasks did not complete in time.")
        assertEquals(totalRegistrations, PaymentMethodProvider.getFactoriesCount())
    }

    @Test
    fun `when register for stored is called concurrently from multiple threads, then provider should not lose any data`() {
        val threadCount = 10
        val registrationsPerThread = 100
        val totalRegistrations = threadCount * registrationsPerThread
        val executor = Executors.newFixedThreadPool(threadCount)

        // Submit all registration tasks to the thread pool
        for (i in 0 until totalRegistrations) {
            executor.submit {
                PaymentMethodProvider.register("test_method_$i", storedFactory)
            }
        }

        // Wait for all tasks to complete
        executor.shutdown()
        // Giving it a generous timeout to finish all tasks
        val completed = executor.awaitTermination(5, TimeUnit.SECONDS)
        assertTrue(completed, "Executor tasks did not complete in time.")
        assertEquals(totalRegistrations, PaymentMethodProvider.getStoredFactoriesCount())
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
                application = generateApplication(),
                paymentMethod = PaymentMethod(type = "txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
            assertEquals(1, PaymentMethodProvider.getFactoriesCount())
            assertEquals(secondaryComponent, actualComponent)
            assertNotSame(component, actualComponent)
        }

    @Test
    fun `when register is called with an existing stored txVariant, then the factory is overwritten`() =
        runTest {
            val secondaryComponent = TestPaymentComponent()
            val secondaryFactory = generateStoredFactory(
                paymentComponent = secondaryComponent,
            )

            PaymentMethodProvider.register("txVariant", storedFactory)
            PaymentMethodProvider.register("txVariant", secondaryFactory)

            val actualComponent = PaymentMethodProvider.get(
                application = generateApplication(),
                paymentMethod = StoredPaymentMethod(type = "txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
            assertEquals(1, PaymentMethodProvider.getStoredFactoriesCount())
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
    fun `when register for stored is called for different txVariants, then all factories are stored`() {
        PaymentMethodProvider.register("txVariant_one", storedFactory)
        PaymentMethodProvider.register("txVariant_two", storedFactory)

        assertEquals(2, PaymentMethodProvider.getStoredFactoriesCount())
    }

    @Test
    fun `when get is called for a registered factory, then the correct component is returned`() =
        runTest {
            PaymentMethodProvider.register("txVariant", factory)

            val actualComponent = PaymentMethodProvider.get(
                application = generateApplication(),
                paymentMethod = PaymentMethod(type = "txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
            assertEquals(1, PaymentMethodProvider.getFactoriesCount())
            assertSame(component, actualComponent)
        }

    @Test
    fun `when get is called for a registered stored factory, then the correct component is returned`() =
        runTest {
            PaymentMethodProvider.register("txVariant", storedFactory)

            val actualComponent = PaymentMethodProvider.get(
                application = generateApplication(),
                paymentMethod = StoredPaymentMethod(type = "txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
            assertEquals(1, PaymentMethodProvider.getStoredFactoriesCount())
            assertSame(component, actualComponent)
        }

    @Test
    fun `when get is called for an unregistered factory, then an error is thrown`() = runTest {
        assertThrows<IllegalStateException> {
            PaymentMethodProvider.get(
                application = generateApplication(),
                paymentMethod = PaymentMethod(type = "unregistered_txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
        }
    }

    @Test
    fun `when get is called for an unregistered stored factory, then an error is thrown`() = runTest {
        assertThrows<IllegalStateException> {
            PaymentMethodProvider.get(
                application = generateApplication(),
                paymentMethod = StoredPaymentMethod(type = "unregistered_txVariant", name = "name"),
                coroutineScope = this,
                analyticsManager = TestAnalyticsManager(),
                checkoutConfiguration = generateCheckoutConfiguration(),
                componentParamsBundle = generateComponentParamsBundle(),
                checkoutCallbacks = CheckoutCallbacks(),
            )
        }
    }

    @Test
    fun `when clear is called, then all factories are removed`() {
        PaymentMethodProvider.register("txVariant_one", factory)
        PaymentMethodProvider.register("txVariant_two", factory)
        PaymentMethodProvider.register("txVariant_three", storedFactory)
        assertEquals(2, PaymentMethodProvider.getFactoriesCount())
        assertEquals(1, PaymentMethodProvider.getStoredFactoriesCount())

        PaymentMethodProvider.clear()

        assertEquals(0, PaymentMethodProvider.getFactoriesCount())
        assertEquals(0, PaymentMethodProvider.getStoredFactoriesCount())
    }

    private fun generateFactory(paymentComponent: PaymentComponent<BasePaymentComponentState>) =
        object :
            PaymentComponentFactory<BasePaymentComponentState, PaymentComponent<BasePaymentComponentState>> {
            override fun create(
                application: Application,
                paymentMethod: PaymentMethod,
                coroutineScope: CoroutineScope,
                analyticsManager: AnalyticsManager,
                checkoutConfiguration: CheckoutConfiguration,
                componentParamsBundle: ComponentParamsBundle,
                checkoutCallbacks: CheckoutCallbacks,
            ) = paymentComponent
        }

    private fun generateStoredFactory(paymentComponent: PaymentComponent<BasePaymentComponentState>) =
        object :
            StoredPaymentComponentFactory<BasePaymentComponentState, PaymentComponent<BasePaymentComponentState>> {
            override fun create(
                application: Application,
                storedPaymentMethod: StoredPaymentMethod,
                coroutineScope: CoroutineScope,
                analyticsManager: AnalyticsManager,
                checkoutConfiguration: CheckoutConfiguration,
                componentParamsBundle: ComponentParamsBundle,
                checkoutCallbacks: CheckoutCallbacks,
            ) = paymentComponent
        }

    private fun generateApplication(): Application {
        return org.mockito.Mockito.mock(Application::class.java)
    }

    private fun generateCheckoutConfiguration() = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfgh",
    )
}
