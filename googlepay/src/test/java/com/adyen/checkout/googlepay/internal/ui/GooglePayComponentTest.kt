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
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.error.internal.PaymentMethodUnavailableError
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
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
    fun `when setLoading is called with true, then the button is disabled`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        component.setLoading(true)

        val buttonViewState = requireNotNull(viewState.latestValue.buttonViewState)
        assertTrue(buttonViewState.isLoading)
    }

    @Test
    fun `when submit is called, then the button is disabled`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        component.submit()

        val buttonViewState = requireNotNull(viewState.latestValue.buttonViewState)
        assertTrue(buttonViewState.isLoading)
    }

    @Test
    fun `when submit is called, then no payment event is emitted before a result`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.submit()

        assertTrue(events.values.isEmpty())
    }

    @Test
    fun `when submit is called, then Pay event is emitted for the view to handle`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.viewEventFlow.test(testScheduler)
        // although the flow itself is not tested, this line is needed to ensure the flow gets updated
        component.viewState.test(testScheduler)

        component.submit()

        assertEquals(1, events.values.size)
        assertInstanceOf<GooglePayViewEvent.Pay>(events.latestValue)
    }

    @Test
    fun `when availability check returns false and submit is called afterwards, then no event is emitted for the view`() =
        runTest {
            val component = createComponent(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
                googlePayAvailabilityCheck = mockGooglePayAvailabilityCheck(isAvailable = false),
            )
            val events = component.viewEventFlow.test(testScheduler)
            // although the flow itself is not tested, this line is needed to ensure the flow gets updated
            component.viewState.test(testScheduler)

            component.submit()

            assertTrue(events.values.isEmpty())
        }

    @Test
    fun `when result is successful with valid data, then a Submit event is emitted`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.SUCCESS, createPaymentData()))

        assertEquals(1, events.values.size)
        assertInstanceOf<PaymentComponentEvent.Submit>(events.latestValue)
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
        assertInstanceOf<PaymentComponentEvent.Error>(events.latestValue)
    }

    @Test
    fun `when result is canceled, then no event is emitted and loading is reset`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)
        val viewState = component.viewState.test(testScheduler)
        component.submit()

        component.onPaymentResult(createResult(CommonStatusCodes.CANCELED))

        assertTrue(events.values.isEmpty())
        val buttonViewState = requireNotNull(viewState.latestValue.buttonViewState)
        assertFalse(buttonViewState.isLoading)
    }

    @Test
    fun `when result is an error, then an Error event is emitted`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val events = component.eventFlow.test(testScheduler)

        component.onPaymentResult(createResult(CommonStatusCodes.INTERNAL_ERROR))

        assertEquals(1, events.values.size)
        assertInstanceOf<PaymentComponentEvent.Error>(events.latestValue)
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
    fun `when availability check returns true, then the button is shown`() = runTest {
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            googlePayAvailabilityCheck = mockGooglePayAvailabilityCheck(isAvailable = true),
        )
        val viewState = component.viewState.test(testScheduler)

        assertNotNull(viewState.latestValue.buttonViewState)
    }

    @Test
    fun `when availability check returns false, then the button is not shown`() = runTest {
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            googlePayAvailabilityCheck = mockGooglePayAvailabilityCheck(isAvailable = false),
        )
        val viewState = component.viewState.test(testScheduler)

        assertNull(viewState.latestValue.buttonViewState)
    }

    @Test
    fun `when availability check returns false, then an Error event is emitted`() = runTest {
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            googlePayAvailabilityCheck = mockGooglePayAvailabilityCheck(isAvailable = false),
        )
        val events = component.eventFlow.test(testScheduler)

        assertEquals(1, events.values.size)
        val errorEvent = assertInstanceOf<PaymentComponentEvent.Error>(events.latestValue)
        val nestedError = assertInstanceOf<PaymentMethodUnavailableError>(errorEvent.error)
        assertEquals("Google Pay is not available", nestedError.message)
    }

    @Test
    fun `when component is created, then the button view state exposes allowed payment methods and styling`() =
        runTest {
            val buttonStyling = GooglePayButtonStyling()
            val component = createComponent(
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
                googlePayButtonStyling = buttonStyling,
            )
            val viewState = component.viewState.test(testScheduler)

            val buttonViewState = requireNotNull(viewState.latestValue.buttonViewState)
            assertTrue(buttonViewState.allowedPaymentMethods.isNotEmpty())
            assertEquals(buttonStyling, buttonViewState.buttonStyling)
        }

    private fun createComponent(
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager = mock(),
        googlePayAvailabilityCheck: GooglePayAvailabilityCheck = mockGooglePayAvailabilityCheck(isAvailable = true),
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ): GooglePayComponent {
        val componentParams = createComponentParams(googlePayButtonStyling)
        return GooglePayComponent(
            analyticsManager = analyticsManager,
            componentParams = componentParams,
            sdkDataProvider = mock<SdkDataProvider>(),
            googlePayAvailabilityCheck = googlePayAvailabilityCheck,
            paymentMethodType = "googlepay",
            componentStateValidator = GooglePayComponentStateValidator(),
            componentStateFactory = GooglePayComponentStateFactory(componentParams),
            componentStateReducer = GooglePayComponentStateReducer(),
            viewStateProducer = GooglePayViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }

    private fun createComponentParams(
        googlePayButtonStyling: GooglePayButtonStyling? = null,
    ) = GooglePayComponentParams(
        amount = Amount("USD", 0),
        gatewayMerchantId = "TEST_GATEWAY_MERCHANT_ID",
        googlePayEnvironment = WalletConstants.ENVIRONMENT_TEST,
        totalPriceStatus = "FINAL",
        countryCode = null,
        merchantInfo = null,
        allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
        allowedCardNetworks = listOf("VISA", "MASTERCARD"),
        isAllowPrepaidCards = false,
        isAllowCreditCards = null,
        isAssuranceDetailsRequired = null,
        isEmailRequired = false,
        isExistingPaymentMethodRequired = false,
        isShippingAddressRequired = false,
        shippingAddressParameters = null,
        isBillingAddressRequired = false,
        billingAddressParameters = null,
        checkoutOption = null,
        googlePayButtonStyling = googlePayButtonStyling,
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

    private fun mockGooglePayAvailabilityCheck(isAvailable: Boolean): GooglePayAvailabilityCheck {
        return mock {
            onBlocking { isAvailable() } doReturn isAvailable
        }
    }

    companion object {
        private const val TEST_PAYMENT_DATA_JSON =
            "{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}"
    }
}
