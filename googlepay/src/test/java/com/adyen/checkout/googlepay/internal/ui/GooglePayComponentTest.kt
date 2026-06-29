/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsEvent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class GooglePayComponentTest {

    @Test
    fun `when requiresUserInteraction is called, then it returns true`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        assertTrue(component.requiresUserInteraction())
    }

    @Test
    fun `when setLoading is called with true, then the loading state is updated`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        component.setLoading(true)

        assertTrue(viewState.latestValue.isLoading)
    }

    @Test
    fun `when submit is called, then loading state is set to true`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        component.submit()

        assertTrue(viewState.latestValue.isLoading)
    }

    @Test
    fun `when submit is called, then no payment event is emitted before a result`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.submit()

        assertTrue(events.values.isEmpty())
    }

    @Test
    fun `when result is successful with valid data, then a Submit event is emitted`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.SUCCESS, createPaymentData()))

        assertEquals(1, events.values.size)
        assertInstanceOf(PaymentComponentEvent.Submit::class.java, events.latestValue)
    }

    @Test
    fun `when result is successful with valid data, then a submit analytics event is tracked`() = runTest {
        val analyticsManager = mock<AnalyticsManager>()
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            analyticsManager = analyticsManager,
        )
        component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.SUCCESS, createPaymentData()))

        val captor = argumentCaptor<AnalyticsEvent>()
        verify(analyticsManager, atLeastOnce()).trackEvent(captor.capture())
        assertTrue(
            captor.allValues.any { event ->
                event is AnalyticsEvent.Log && event.type == AnalyticsEvent.Log.Type.SUBMIT
            },
        )
    }

    @Test
    fun `when result is successful but data is missing, then an Error event is emitted`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.SUCCESS, paymentData = null))

        assertEquals(1, events.values.size)
        assertInstanceOf(PaymentComponentEvent.Error::class.java, events.latestValue)
    }

    @Test
    fun `when result is canceled, then no event is emitted and loading is reset`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)
        val viewState = component.viewState.test(testScheduler)
        component.submit()

        component.onPaymentResult(createResult(CommonStatusCodes.CANCELED))

        assertTrue(events.values.isEmpty())
        assertFalse(viewState.latestValue.isLoading)
    }

    @Test
    fun `when result is an error, then an Error event is emitted`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.INTERNAL_ERROR))

        assertEquals(1, events.values.size)
        assertInstanceOf(PaymentComponentEvent.Error::class.java, events.latestValue)
    }

    @Test
    fun `when result is an error, then a third party error analytics event is tracked`() = runTest {
        val analyticsManager = mock<AnalyticsManager>()
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            analyticsManager = analyticsManager,
        )
        component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.INTERNAL_ERROR))

        val captor = argumentCaptor<AnalyticsEvent>()
        verify(analyticsManager, atLeastOnce()).trackEvent(captor.capture())
        assertTrue(
            captor.allValues.any { event ->
                event is AnalyticsEvent.Error && event.errorType == AnalyticsEvent.Error.Type.THIRD_PARTY
            },
        )
    }

    @Test
    fun `when availability check returns true, then the available state is updated`() = runTest {
        val availabilityCheck = mock<GooglePayAvailabilityCheck> {
            onBlocking { isAvailable() } doReturn true
        }
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            googlePayAvailabilityCheck = availabilityCheck,
        )
        val viewState = component.viewState.test(testScheduler)

        assertTrue(viewState.latestValue.isAvailable)
    }

    @Test
    fun `when availability check returns false, then the available state stays false`() = runTest {
        val availabilityCheck = mock<GooglePayAvailabilityCheck> {
            onBlocking { isAvailable() } doReturn false
        }
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            googlePayAvailabilityCheck = availabilityCheck,
        )
        val viewState = component.viewState.test(testScheduler)

        assertFalse(viewState.latestValue.isAvailable)
    }

    private fun createComponent(
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager = mock(),
        googlePayAvailabilityCheck: GooglePayAvailabilityCheck = mock {
            onBlocking { isAvailable() } doReturn false
        },
    ) = GooglePayComponent(
        analyticsManager = analyticsManager,
        componentParams = mock<GooglePayComponentParams>(),
        sdkDataProvider = mock<SdkDataProvider>(),
        googlePayAvailabilityCheck = googlePayAvailabilityCheck,
        paymentMethodType = "googlepay",
        componentStateValidator = GooglePayComponentStateValidator(),
        componentStateFactory = GooglePayComponentStateFactory(),
        componentStateReducer = GooglePayComponentStateReducer(),
        viewStateProducer = GooglePayViewStateProducer(),
        coroutineScope = coroutineScope,
    )

    private fun createResult(
        statusCode: Int,
        paymentData: PaymentData? = null,
    ): ApiTaskResult<PaymentData> {
        val status = mock<Status> {
            on { this.statusCode } doReturn statusCode
        }
        return mock {
            on { this.status } doReturn status
            on { result } doReturn paymentData
        }
    }

    private fun createPaymentData() = mock<PaymentData> {
        on { toJson() } doReturn TEST_PAYMENT_DATA_JSON
    }

    companion object {
        private const val TEST_PAYMENT_DATA_JSON =
            "{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}"
    }
}
