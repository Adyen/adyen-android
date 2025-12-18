/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.provider.TestSdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.payto.PayToConfiguration
import com.adyen.checkout.payto.getPayToConfiguration
import com.adyen.checkout.payto.payTo
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class)
internal class StoredPayToDelegateTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var sdkDataProvider: TestSdkDataProvider
    private lateinit var delegate: PayToDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        sdkDataProvider = TestSdkDataProvider()
        delegate = createDelegate()
    }

    @Test
    fun `when delegate is initialized, then state is valid`() = runTest {
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        with(testFlow.latestValue) {
            assertTrue(isInputValid)
            assertTrue(isReady)
            assertTrue(isValid)
        }
    }

    @Test
    fun `when delegate is initialized, then submit handler onSubmit is called`() = runTest {
        val testFlow = delegate.submitFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals(delegate.componentStateFlow.first(), testFlow.latestValue)
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            analyticsManager.assertIsInitialized()
        }

        @Test
        fun `when delegate is initialized, then render event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.rendered(
                component = TEST_PAYMENT_METHOD_TYPE,
                isStoredPaymentMethod = true,
            )
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when delegeate is initialized, the component state is submitted, then submit event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain sdkData`() = runTest {
            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertEquals(TestSdkDataProvider.TEST_SDK_DATA, testFlow.latestValue.data.paymentMethod?.sdkData)
        }
    }

    private fun createDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = StoredPayToDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getPayToConfiguration(),
        ),
        storedPaymentMethod = StoredPaymentMethod(
            id = STORED_ID,
            type = TEST_PAYMENT_METHOD_TYPE,
        ),
        order = TEST_ORDER,
        analyticsManager = analyticsManager,
        sdkDataProvider = sdkDataProvider,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: PayToConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        payTo(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val STORED_ID = "stored_id"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
    }
}
