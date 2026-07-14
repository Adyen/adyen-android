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
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    private fun createComponent(): StoredBlikComponent {
        return StoredBlikComponent(
            storedPaymentMethod = storedPaymentMethod,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
        )
    }

    companion object {
        private const val PAYMENT_METHOD_TYPE = "blik"
    }
}
