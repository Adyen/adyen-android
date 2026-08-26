/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/6/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.GenericPaymentComponentState
import com.adyen.checkout.core.components.internal.ui.state.GenericViewStateProducer
import com.adyen.checkout.core.components.paymentmethod.GenericDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class GenericPaymentComponentTest(
    @param:Mock private val sdkDataProvider: SdkDataProvider,
) {

    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun setUp() {
        analyticsManager = TestAnalyticsManager()
    }

    @Test
    fun `when component is initialized then rendered event is tracked`() {
        // WHEN
        createComponent()

        // THEN
        val expected = GenericEvents.rendered(component = TEST_PAYMENT_METHOD_TYPE)
        analyticsManager.assertHasEventEquals(expected)
    }

    @Test
    fun `when submit is called, then eventFlow emits Submit event`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        assertEquals(1, events.values.size)
        assertInstanceOf(PaymentComponentEvent.Submit::class.java, events.latestValue)
    }

    @Test
    fun `when submit is called, then state contains correct payment method type`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        val state = (events.latestValue as PaymentComponentEvent.Submit).state
        assertInstanceOf(GenericPaymentComponentState::class.java, state)
        val details = (state as GenericPaymentComponentState).data.paymentMethod
        assertInstanceOf(GenericDetails::class.java, details)
        assertEquals(TEST_PAYMENT_METHOD_TYPE, details!!.type)
    }

    @Test
    fun `when submit is called, then state contains sdk data from provider`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        val state = (events.latestValue as PaymentComponentEvent.Submit).state as GenericPaymentComponentState
        assertEquals(TEST_SDK_DATA, state.data.paymentMethod!!.sdkData)
    }

    @Test
    fun `when submit is called, then state subtype is null`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        val state = (events.latestValue as PaymentComponentEvent.Submit).state as GenericPaymentComponentState
        assertNull(state.data.paymentMethod!!.subtype)
    }

    @Test
    fun `when submit is called, then state order is null`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        val state = (events.latestValue as PaymentComponentEvent.Submit).state as GenericPaymentComponentState
        assertNull(state.data.order)
    }

    @Test
    fun `when submit is called, then state is valid`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        val state = (events.latestValue as PaymentComponentEvent.Submit).state as GenericPaymentComponentState
        assertTrue(state.isValid)
    }

    @Test
    fun `when submit is called, then sdk data provider is called`() = runTest {
        // GIVEN
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn TEST_SDK_DATA
        val component = createComponent()
        component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        verify(sdkDataProvider).createEncodedSdkData()
    }

    @Test
    fun `when requiresUserInteraction is called, then it returns false`() {
        // GIVEN
        val component = createComponent()

        // WHEN
        val result = component.requiresUserInteraction()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `when component is created, then the pay button is shown`() = runTest {
        // GIVEN
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        // WHEN
        val viewState = component.viewState.test(testScheduler)

        // THEN
        assertNotNull(viewState.latestValue.payButtonViewState)
    }

    @Test
    fun `when show submit button is false, then the pay button is not shown`() = runTest {
        // GIVEN
        val component = createComponent(
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            showSubmitButton = false,
        )

        // WHEN
        val viewState = component.viewState.test(testScheduler)

        // THEN
        assertNull(viewState.latestValue.payButtonViewState)
    }

    @Test
    fun `when setLoading is called with true, then the button is loading`() = runTest {
        // GIVEN
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        // WHEN
        component.setLoading(true)

        // THEN
        assertTrue(viewState.latestValue.isLoading)
        assertTrue(requireNotNull(viewState.latestValue.payButtonViewState).isLoading)
    }

    @Test
    fun `when setLoading is called with false, then the button is not loading`() = runTest {
        // GIVEN
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)
        component.setLoading(true)

        // WHEN
        component.setLoading(false)

        // THEN
        assertFalse(viewState.latestValue.isLoading)
        assertFalse(requireNotNull(viewState.latestValue.payButtonViewState).isLoading)
    }

    private fun createComponent(
        coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        showSubmitButton: Boolean = true,
    ): GenericPaymentComponent {
        return GenericPaymentComponent(
            analyticsManager = analyticsManager,
            paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
            sdkDataProvider = sdkDataProvider,
            componentStateValidator = GenericComponentStateValidator(),
            componentStateFactory = GenericComponentStateFactory(),
            componentStateReducer = GenericComponentStateReducer(),
            viewStateProducer = GenericViewStateProducer(
                amount = TEST_AMOUNT,
                showSubmitButton = showSubmitButton,
            ),
            coroutineScope = coroutineScope,
        )
    }

    companion object {
        private const val TEST_PAYMENT_METHOD_TYPE = "test_payment_method"
        private const val TEST_SDK_DATA = "test_sdk_data"
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
