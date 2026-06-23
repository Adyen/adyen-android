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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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
    }

    @Nested
    @DisplayName("when sending events and")
    inner class SendEventTest {

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
    }

    @Test
    fun `when timer ticks, then all stored events should be sent`() = runTest {
        whenever(analyticsRepository.setup()) doReturn "test value"
        whenever(analyticsRepository.storeEvent(any())) doReturn Unit
        whenever(analyticsRepository.sendEvents(any())) doReturn Unit
        val analyticsManager = createAnalyticsManager(coroutineScope = backgroundScope)

        analyticsManager.trackEvent(GenericEvents.rendered("dropin", false))
        testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS + 1.milliseconds)

        verify(analyticsRepository, times(1)).sendEvents(any())
    }

    @Test
    fun `when analytics level is initial, then setup is not called`() = runTest {
        createAnalyticsManager(analyticsParamsLevel = AnalyticsParamsLevel.INITIAL)

        verify(analyticsRepository, never()).setup()
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
