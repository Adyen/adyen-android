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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
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

    private lateinit var analyticsManager: DefaultAnalyticsManager

    @BeforeEach
    fun setup() {
        analyticsManager = createAnalyticsManager()
    }

    @Nested
    @DisplayName("when initializing and")
    inner class InitializeTest {

        @Test
        fun `initialize is called twice, then the second time is ignored`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doAnswer { error("test") }

            analyticsManager.initialize(this@InitializeTest, this)

            verify(analyticsRepository, times(1)).fetchCheckoutAttemptId()
            analyticsManager.clear(this@InitializeTest)
        }
    }

    @Nested
    @DisplayName("when tracking events and")
    inner class TrackEventTest {

        @Test
        fun `analytics level is initial, then events should not be stored`() = runTest {
            analyticsManager = createAnalyticsManager(AnalyticsParamsLevel.INITIAL)
            analyticsManager.initialize(this@TrackEventTest, this)

            analyticsManager.trackEvent(GenericEvents.rendered("dropin", false))

            verify(analyticsRepository, never()).storeEvent(any())
            analyticsManager.clear(this@TrackEventTest)
        }

        @Test
        fun `sending events is enabled, then events should be stored`() = runTest {
            analyticsManager.initialize(this@TrackEventTest, this)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = false,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).storeEvent(event)
            analyticsManager.clear(this@TrackEventTest)
        }

        @Test
        fun `the event should force sending, then the event is sent right away`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doReturn "test value"
            analyticsManager.initialize(this@TrackEventTest, this)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository).sendEvents(any())
            analyticsManager.clear(this@TrackEventTest)
        }
    }

    @Nested
    @DisplayName("when sending events and")
    inner class SendEventTest {

        @Test
        fun `analytics level is initial, then events are not sent`() = runTest {
            analyticsManager = createAnalyticsManager(AnalyticsParamsLevel.INITIAL)
            analyticsManager.initialize(this@SendEventTest, this)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository, never()).sendEvents(any())
            analyticsManager.clear(this@SendEventTest)
        }
    }

    @Test
    fun `when timer ticks, then all stored events should be sent`() = runTest {
        analyticsManager = createAnalyticsManager(coroutineDispatcher = StandardTestDispatcher(testScheduler))
        whenever(analyticsRepository.fetchCheckoutAttemptId()) doReturn "test value"
        whenever(analyticsRepository.storeEvent(any())) doReturn Unit
        whenever(analyticsRepository.sendEvents(any())) doReturn Unit
        analyticsManager.initialize(this@DefaultAnalyticsManagerTest, this)

        analyticsManager.trackEvent(GenericEvents.rendered("dropin", false))
        testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_SECONDS + 1.milliseconds)

        verify(analyticsRepository, times(1)).sendEvents(any())
        analyticsManager.clear(this@DefaultAnalyticsManagerTest)
    }

    private fun createAnalyticsManager(
        analyticsParamsLevel: AnalyticsParamsLevel = AnalyticsParamsLevel.ALL,
        coroutineDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) = DefaultAnalyticsManager(
        analyticsRepository = analyticsRepository,
        analyticsParams = AnalyticsParams(analyticsParamsLevel),
        checkoutAttemptId = "test-id",
        coroutineDispatcher = coroutineDispatcher,
    )
}
