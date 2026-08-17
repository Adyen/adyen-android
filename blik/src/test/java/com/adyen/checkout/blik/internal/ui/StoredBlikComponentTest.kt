/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2026.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.GenericViewStateProducer
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class StoredBlikComponentTest(
    @param:Mock private val storedPaymentMethod: StoredPaymentMethod,
    @param:Mock private val sdkDataProvider: SdkDataProvider,
) {

    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
    }

    @Test
    fun `when component is initialized then rendered event is tracked`() {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE

        // WHEN
        createComponent()

        // THEN
        val expected = GenericEvents.rendered(
            component = PAYMENT_METHOD_TYPE,
            isStoredPaymentMethod = true,
        )
        analyticsManager.assertHasEventEquals(expected)
    }

    @Test
    fun `when submit is called then Submit event is emitted`() = runTest {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
        whenever(storedPaymentMethod.id) doReturn "stored_pm_id"
        whenever(sdkDataProvider.createEncodedSdkData()) doReturn "sdk_data"
        val component = createComponent()
        val eventFlow = component.eventFlow.test(testScheduler)

        // WHEN
        component.submit()

        // THEN
        assertEquals(1, eventFlow.values.size)
        val event = eventFlow.latestValue
        assertTrue(event is PaymentComponentEvent.Submit)
        assertTrue((event as PaymentComponentEvent.Submit).state.isValid)
    }

    @Test
    fun `when requiresUserInteraction is called then returns false`() {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
        val component = createComponent()

        // WHEN
        val result = component.requiresUserInteraction()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `when component is created then the pay button is shown`() = runTest {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        // WHEN
        val viewState = component.viewState.test(testScheduler)

        // THEN
        assertNotNull(viewState.latestValue.payButtonViewState)
    }

    @Test
    fun `when show submit button is false then the pay button is not shown`() = runTest {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
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
    fun `when setLoading is called with true then the button is loading`() = runTest {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        // WHEN
        component.setLoading(true)

        // THEN
        assertTrue(viewState.latestValue.isLoading)
        assertTrue(requireNotNull(viewState.latestValue.payButtonViewState).isLoading)
    }

    @Test
    fun `when setLoading is called with false then the button is not loading`() = runTest {
        // GIVEN
        whenever(storedPaymentMethod.type) doReturn PAYMENT_METHOD_TYPE
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
    ): StoredBlikComponent {
        return StoredBlikComponent(
            storedPaymentMethod = storedPaymentMethod,
            analyticsManager = analyticsManager,
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
        private const val PAYMENT_METHOD_TYPE = "blik"
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
