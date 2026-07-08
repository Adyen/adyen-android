/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2026.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateFactory
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateReducer
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateValidator
import com.adyen.checkout.blik.internal.ui.state.BlikViewStateProducer
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class BlikComponentTest(
    @param:Mock private val sdkDataProvider: SdkDataProvider,
    @param:Mock private val componentStateValidator: BlikComponentStateValidator,
) {

    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
    }

    @Test
    fun `when component is initialized then rendered event is tracked`() {
        // WHEN
        createComponent()

        // THEN
        val expected = GenericEvents.rendered(component = PaymentMethodTypes.BLIK)
        analyticsManager.assertHasEventEquals(expected)
    }

    @Nested
    @DisplayName("when submit is called")
    inner class SubmitTest {

        @Test
        fun `and state is valid then Submit event is emitted`() = runTest {
            // GIVEN
            whenever(componentStateValidator.validate(any())).thenAnswer { it.arguments[0] }
            whenever(componentStateValidator.isValid(any())).thenReturn(true)
            whenever(sdkDataProvider.createEncodedSdkData()).thenReturn("sdk_data")
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
        fun `and state is invalid then no Submit event is emitted`() = runTest {
            // GIVEN
            whenever(componentStateValidator.validate(any())).thenAnswer { it.arguments[0] }
            whenever(componentStateValidator.isValid(any())).thenReturn(false)
            val component = createComponent()
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertTrue(eventFlow.values.isEmpty())
        }
    }

    @Test
    fun `when requiresUserInteraction is called then returns true`() {
        // GIVEN
        whenever(componentStateValidator.validate(any())).thenAnswer { it.arguments[0] }
        val component = createComponent()

        // WHEN
        val result = component.requiresUserInteraction()

        // THEN
        assertTrue(result)
    }

    private fun createComponent(): BlikComponent {
        return BlikComponent(
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            componentStateValidator = componentStateValidator,
            componentStateFactory = BlikComponentStateFactory(),
            componentStateReducer = BlikComponentStateReducer(),
            viewStateProducer = BlikViewStateProducer(),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        )
    }
}
