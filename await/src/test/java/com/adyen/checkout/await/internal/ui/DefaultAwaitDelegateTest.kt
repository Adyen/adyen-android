/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/8/2022.
 */

package com.adyen.checkout.await.internal.ui

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.components.core.internal.test.TestStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class)
internal class DefaultAwaitDelegateTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var statusRepository: TestStatusRepository
    private lateinit var paymentDataRepository: PaymentDataRepository
    private lateinit var delegate: DefaultAwaitDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        statusRepository = TestStatusRepository()
        paymentDataRepository = PaymentDataRepository(SavedStateHandle())
        delegate = createDelegate()
    }

    @Test
    fun `when  polling status, then output data will be emitted`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "pending")),
            Result.success(StatusResponse(resultCode = "finished")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val outputDataFlow = delegate.outputDataFlow.test(testScheduler)

        delegate.handleAction(
            AwaitAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            ),
            Activity(),
        )

        // We skip the first output data value as it's the initial value

        with(outputDataFlow.values[1]) {
            assertFalse(isValid)
            assertEquals(TEST_PAYMENT_METHOD_TYPE, paymentMethodType)
        }

        with(outputDataFlow.values[2]) {
            assertTrue(isValid)
            assertEquals(TEST_PAYMENT_METHOD_TYPE, paymentMethodType)
        }
    }

    @Test
    fun `when polling is final, then details will be emitted`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val detailsFlow = delegate.detailsFlow.test(testScheduler)

        delegate.handleAction(
            AwaitAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            ),
            Activity(),
        )

        val expectedDetails = JSONObject().apply {
            put(DefaultAwaitDelegate.PAYLOAD_DETAILS_KEY, "testpayload")
        }

        with(detailsFlow.latestValue) {
            assertEquals(expectedDetails.toString(), details.toString())
            assertEquals(TEST_PAYMENT_DATA, paymentData)
        }
    }

    @Test
    fun `when polling fails, then an error is propagated`() = runTest {
        val error = IOException("test")
        statusRepository.pollingResults = listOf(Result.failure(error))
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(
            AwaitAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            ),
            Activity(),
        )

        assertEquals(error, exceptionFlow.latestValue.cause)
    }

    @Test
    fun `when polling is final and payload is empty, then an error is propagated`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "")),
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(
            AwaitAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            ),
            Activity(),
        )

        assertTrue(exceptionFlow.latestValue is ComponentException)
        assertEquals("Payment was not completed. - finished", exceptionFlow.latestValue.message)
    }

    @Test
    fun `when a wrongly typed action is used, then an error is propagated`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(RedirectAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

        assertTrue(exceptionFlow.latestValue is ComponentException)
        assertEquals("Unsupported action", exceptionFlow.latestValue.message)
    }

    @Test
    fun `when payment data is null, then an error is propagated`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = null), Activity())

        assertTrue(exceptionFlow.latestValue is ComponentException)
        assertEquals("Payment data is null", exceptionFlow.latestValue.message)
    }

    @Test
    fun `when initializing and action is set, then state is restored`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle().apply {
            set(DefaultAwaitDelegate.ACTION_KEY, AwaitAction(paymentMethodType = "test", paymentData = "paymentData"))
        }
        delegate = createDelegate(savedStateHandle)
        val detailsFlow = delegate.detailsFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertTrue(detailsFlow.values.isNotEmpty())
    }

    @Test
    fun `when details are emitted, then state is cleared`() = runTest {
        statusRepository.pollingResults = listOf(
            Result.success(StatusResponse(resultCode = "finished", payload = "testpayload")),
        )
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = "paymentData"), Activity())

        assertNull(savedStateHandle[DefaultAwaitDelegate.ACTION_KEY])
    }

    @Test
    fun `when an error is emitted, then state is cleared`() = runTest {
        val savedStateHandle = SavedStateHandle()
        delegate = createDelegate(savedStateHandle)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.handleAction(AwaitAction(paymentMethodType = "test", paymentData = null), Activity())

        assertNull(savedStateHandle[DefaultAwaitDelegate.ACTION_KEY])
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when handleAction is called, then action event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val action = AwaitAction(
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                type = TEST_ACTION_TYPE,
                paymentData = TEST_PAYMENT_DATA,
            )

            delegate.handleAction(action, Activity())

            val expectedEvent = GenericEvents.action(
                component = TEST_PAYMENT_METHOD_TYPE,
                subType = TEST_ACTION_TYPE,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun createDelegate(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): DefaultAwaitDelegate {
        val configuration = CheckoutConfiguration(
            Environment.TEST,
            TEST_CLIENT_KEY,
        )

        return DefaultAwaitDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
            analyticsManager = analyticsManager,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_ACTION_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private const val TEST_PAYMENT_DATA = "TEST_PAYMENT_DATA"
    }
}
