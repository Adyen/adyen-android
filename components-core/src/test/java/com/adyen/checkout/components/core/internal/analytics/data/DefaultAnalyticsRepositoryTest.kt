package com.adyen.checkout.components.core.internal.analytics.data

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.DirectAnalyticsEventCreation
import com.adyen.checkout.components.core.internal.analytics.data.local.AnalyticsLocalDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsRemoteDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsSetupProvider
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class DefaultAnalyticsRepositoryTest(
    @Mock private val localInfoDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Info>,
    @Mock private val localLogDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Log>,
    @Mock private val localErrorDataStore: AnalyticsLocalDataStore<AnalyticsEvent.Error>,
    @Mock private val remoteDataStore: AnalyticsRemoteDataStore,
    @Mock private val analyticsSetupProvider: AnalyticsSetupProvider,
    @Mock private val analyticsTrackRequestProvider: AnalyticsTrackRequestProvider,
) {

    private lateinit var analyticsRepository: DefaultAnalyticsRepository

    @BeforeEach
    fun setup() {
        analyticsRepository = DefaultAnalyticsRepository(
            localInfoDataStore = localInfoDataStore,
            localLogDataStore = localLogDataStore,
            localErrorDataStore = localErrorDataStore,
            remoteDataStore = remoteDataStore,
            analyticsSetupProvider = analyticsSetupProvider,
            analyticsTrackRequestProvider = analyticsTrackRequestProvider,
        )
    }

    @Test
    fun `when fetching attempt id, then it should be extracted from the response`() = runTest {
        whenever(analyticsSetupProvider.provide()) doReturn mock()
        val checkoutAttemptId = "some id"
        whenever(remoteDataStore.fetchCheckoutAttemptId(any())) doReturn AnalyticsSetupResponse(checkoutAttemptId)

        val result = analyticsRepository.fetchCheckoutAttemptId()

        assertEquals(checkoutAttemptId, result)
    }

    @OptIn(DirectAnalyticsEventCreation::class)
    @Test
    fun `when storing an event, then the correct data store should be used`() = runTest {
        val infoEvent = AnalyticsEvent.Info(component = "test")
        analyticsRepository.storeEvent(infoEvent)

        verify(localInfoDataStore).storeEvent(infoEvent)

        val logEvent = AnalyticsEvent.Log(component = "test")
        analyticsRepository.storeEvent(logEvent)

        verify(localLogDataStore).storeEvent(logEvent)

        val errorEvent = AnalyticsEvent.Error(component = "test")
        analyticsRepository.storeEvent(errorEvent)

        verify(localErrorDataStore).storeEvent(errorEvent)
    }

    @Nested
    @DisplayName("when sending events and")
    inner class SendEventsTest {

        @Test
        fun `there are no events stored, then sending is canceled`() = runTest {
            whenever(localInfoDataStore.fetchEvents(any())) doReturn emptyList()
            whenever(localLogDataStore.fetchEvents(any())) doReturn emptyList()
            whenever(localErrorDataStore.fetchEvents(any())) doReturn emptyList()

            analyticsRepository.sendEvents("test")

            verify(remoteDataStore, never()).sendEvents(any(), any())
        }

        @OptIn(DirectAnalyticsEventCreation::class)
        @Test
        fun `it is successful, then events are cleared from storage`() = runTest {
            val infoEvents = listOf(AnalyticsEvent.Info(component = "test info"))
            val logEvents = listOf(AnalyticsEvent.Log(component = "test log"))
            val errorEvents = listOf(AnalyticsEvent.Error(component = "test error"))
            whenever(localInfoDataStore.fetchEvents(any())) doReturn infoEvents
            whenever(localLogDataStore.fetchEvents(any())) doReturn logEvents
            whenever(localErrorDataStore.fetchEvents(any())) doReturn errorEvents
            whenever(analyticsTrackRequestProvider.invoke(any(), any(), any())) doReturn mock()

            analyticsRepository.sendEvents("test")

            verify(localInfoDataStore).removeEvents(infoEvents)
            verify(localLogDataStore).removeEvents(logEvents)
            verify(localErrorDataStore).removeEvents(errorEvents)
        }

        @Test
        fun `it fails, then events are not cleared from storage`() = runTest {
            whenever(localInfoDataStore.fetchEvents(any())) doReturn listOf(mock())
            whenever(localLogDataStore.fetchEvents(any())) doReturn listOf(mock())
            whenever(localErrorDataStore.fetchEvents(any())) doReturn listOf(mock())
            whenever(analyticsTrackRequestProvider.invoke(any(), any(), any())) doReturn mock()
            whenever(remoteDataStore.sendEvents(any(), any())) doAnswer { error("test") }

            runCatching {
                analyticsRepository.sendEvents("test")
            }

            verify(localInfoDataStore, never()).removeEvents(any())
            verify(localLogDataStore, never()).removeEvents(any())
            verify(localErrorDataStore, never()).removeEvents(any())
        }
    }
}
