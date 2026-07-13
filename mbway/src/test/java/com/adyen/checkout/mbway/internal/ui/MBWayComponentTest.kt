/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2026.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
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
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class MBWayComponentTest(
    @param:Mock private val sdkDataProvider: SdkDataProvider,
    @param:Mock private val componentStateValidator: MBWayComponentStateValidator,
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
        val expected = GenericEvents.rendered(component = PaymentMethodTypes.MB_WAY)
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

    private fun createComponent(): MBWayComponent {
        return MBWayComponent(
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            componentStateValidator = componentStateValidator,
            componentStateFactory = MBWayComponentStateFactory(Locale("pt", "PT")),
            componentStateReducer = MBWayComponentStateReducer(),
            viewStateProducer = MBWayViewStateProducer(null),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
        )
    }
}
