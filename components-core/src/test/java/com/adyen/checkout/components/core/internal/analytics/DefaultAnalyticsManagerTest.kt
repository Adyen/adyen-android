package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.internal.analytics.data.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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

@OptIn(ExperimentalCoroutinesApi::class, DirectAnalyticsEventCreation::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class, TestDispatcherExtension::class)
internal class DefaultAnalyticsManagerTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var analyticsManager: DefaultAnalyticsManager

    @BeforeEach
    fun setup() {
        analyticsManager = createAnalyticsManager()
    }

    @Test
    fun `checkoutAttemptId is not available by default`() {
        assertEquals(DefaultAnalyticsManager.CHECKOUT_ATTEMPT_ID_NOT_FETCHED, analyticsManager.getCheckoutAttemptId())
    }

    @Nested
    @DisplayName("when initializing and")
    inner class InitializeTest {

        @Test
        fun `fetching checkoutAttemptId succeeds, then it is set`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doReturn "test value"

            analyticsManager.initialize(this@InitializeTest, this)

            assertEquals("test value", analyticsManager.getCheckoutAttemptId())
            analyticsManager.clear(this@InitializeTest)
        }

        @Test
        fun `sending events is disabled, then checkoutAttemptId is still set`() = runTest {
            analyticsManager = createAnalyticsManager(AnalyticsParamsLevel.NONE)
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doReturn "test value"

            analyticsManager.initialize(this@InitializeTest, this)

            assertEquals("test value", analyticsManager.getCheckoutAttemptId())
            analyticsManager.clear(this@InitializeTest)
        }

        @Test
        fun `fetching checkoutAttemptId fails, then checkoutAttemptId is failed`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doAnswer { error("test") }

            analyticsManager.initialize(this@InitializeTest, this)

            assertEquals(DefaultAnalyticsManager.FAILED_CHECKOUT_ATTEMPT_ID, analyticsManager.getCheckoutAttemptId())
            analyticsManager.clear(this@InitializeTest)
        }

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
        fun `sending events is disabled, then events should not be stored`() = runTest {
            analyticsManager = createAnalyticsManager(AnalyticsParamsLevel.NONE)
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
        fun `sending events is disabled, then events are not sent`() = runTest {
            analyticsManager = createAnalyticsManager(AnalyticsParamsLevel.NONE)
            analyticsManager.initialize(this@SendEventTest, this)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository, never()).sendEvents(any())
            analyticsManager.clear(this@SendEventTest)
        }

        @Test
        fun `checkoutAttemptId is null when fetching, then events are not sent`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doReturn null
            analyticsManager.initialize(this@SendEventTest, this)
            val event = AnalyticsEvent.Info(
                component = "test",
                shouldForceSend = true,
            )

            analyticsManager.trackEvent(event)

            verify(analyticsRepository, never()).sendEvents(any())
            analyticsManager.clear(this@SendEventTest)
        }

        @Test
        fun `checkoutAttemptId has failed fetching, then events are not sent`() = runTest {
            whenever(analyticsRepository.fetchCheckoutAttemptId()) doAnswer { error("test") }
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
        testScheduler.advanceTimeBy(DefaultAnalyticsManager.DISPATCH_INTERVAL_MILLIS + 1)

        verify(analyticsRepository, times(1)).sendEvents(any())
        analyticsManager.clear(this@DefaultAnalyticsManagerTest)
    }

    private fun createAnalyticsManager(
        analyticsParamsLevel: AnalyticsParamsLevel = AnalyticsParamsLevel.ALL,
        coroutineDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) = DefaultAnalyticsManager(
        analyticsRepository = analyticsRepository,
        analyticsParams = AnalyticsParams(analyticsParamsLevel, ""),
        coroutineDispatcher = coroutineDispatcher,
    )
}
