/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import com.adyen.checkout.core.analytics.internal.data.AnalyticsRepository
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.TestDispatcherExtension
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, DirectAnalyticsEventCreation::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class, TestDispatcherExtension::class)
internal class DefaultAnalyticsManagerTest(
    @param:Mock private val analyticsRepository: AnalyticsRepository,
) {

    @Nested
    @DisplayName("when initializing and")
    inner class InitializationTest {

        @Test
        fun `analytics level is initial, then setup is not called`() = runTest {
            createAnalyticsManager(analyticsParamsLevel = AnalyticsParamsLevel.INITIAL)

            verify(analyticsRepository, never()).setup()
        }

        @Test
        fun `setup fails, then timer is not started`() = runTest {
            whenever(analyticsRepository.setup()) doAnswer { error("setup failed") }
            createAnalyticsManager(coroutineScope = backgroundScope)

            testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS + 1.milliseconds)

            verify(analyticsRepository, never()).sendEvents(any())
        }
    }

    @Nested
    @DisplayName("when tracking events and")
    inner class TrackEventTest {

        @Test
        fun `analytics level is initial, then events should not be stored`() = runTest {
            val analyticsManager = createAnalyticsManager(analyticsParamsLevel = AnalyticsParamsLevel.INITIAL)

            analyticsManager.trackEvent(GenericEvents.rendered("dropin", false))

            verify(analyticsRepository, never()).storeEvent(any())
        }

        @Test
        fun `sending events is enabled, then events should be stored`() = runTest {
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = false,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).storeEvent(event)
        }

        @Test
        fun `the event should force sending, then the event is sent right away`() = runTest {
            whenever(analyticsRepository.setup()) doReturn "test value"
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).sendEvents(any())
        }

        @Test
        fun `storeEvent fails, then error is handled gracefully`() = runTest {
            whenever(analyticsRepository.storeEvent(any())) doAnswer { error("store failed") }
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = false,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).storeEvent(event)
        }
    }

    @Nested
    @DisplayName("when sending events and")
    inner class SendEventsTest {

        @Test
        fun `analytics level is initial, then events are not sent`() = runTest {
            val analyticsManager = createAnalyticsManager(analyticsParamsLevel = AnalyticsParamsLevel.INITIAL)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository, never()).sendEvents(any())
        }

        @Test
        fun `sendEvents fails, then error is handled gracefully`() = runTest {
            whenever(analyticsRepository.setup()) doReturn "test value"
            whenever(analyticsRepository.sendEvents(any())) doAnswer { error("send failed") }
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).sendEvents(any())
        }

        @Test
        fun `timer ticks, then all stored events should be sent`() = runTest {
            whenever(analyticsRepository.setup()) doReturn "test value"
            whenever(analyticsRepository.storeEvent(any())) doReturn Unit
            whenever(analyticsRepository.sendEvents(any())) doReturn Unit
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)

            analyticsManager.trackEvent(GenericEvents.rendered("dropin", false))
            testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS + 1.milliseconds)

            verify(analyticsRepository, times(1)).sendEvents(any())
        }

        @Test
        fun `force send event is tracked, then timer is restarted`() = runTest {
            whenever(analyticsRepository.setup()) doReturn "test value"
            whenever(analyticsRepository.storeEvent(any())) doReturn Unit
            whenever(analyticsRepository.sendEvents(any())) doReturn Unit
            val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)

            // Advance to halfway through the first timer interval
            testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS / 2)

            // Force send resets the timer
            val forceSendEvent = AnalyticsEvent.Info(component = "test", shouldForceSend = true)
            analyticsManager.trackEvent(forceSendEvent)

            // Advance by less than a full interval from the force send — timer should not have ticked again
            testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS / 2)

            // Only the force send should have triggered sendEvents (once from force send)
            verify(analyticsRepository, times(1)).sendEvents(any())

            // Advance past the full interval from force send — timer ticks
            testScheduler.advanceTimeBy((DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS / 2) + 1.milliseconds)

            verify(analyticsRepository, times(2)).sendEvents(any())
        }
    }

    @Test
    fun `when getCheckoutAttemptId is called, then returns provided id`() = runTest {
        val analyticsManager = createAnalyticsManager(analyticsParamsLevel = AnalyticsParamsLevel.INITIAL)

        assertEquals("test-id", analyticsManager.getCheckoutAttemptId())
    }

    private fun createAnalyticsManager(
        analyticsParamsLevel: AnalyticsParamsLevel = AnalyticsParamsLevel.ALL,
        coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
    ): DefaultAnalyticsManager {
        return DefaultAnalyticsManager(
            analyticsRepository = analyticsRepository,
            analyticsParams = AnalyticsParams(analyticsParamsLevel),
            checkoutAttemptId = "test-id",
            coroutineScope = coroutineScope,
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )
    }
}
