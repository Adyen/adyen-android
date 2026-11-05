/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/8/2022.
 */

package com.adyen.checkout.core.components.internal.data.api

import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.TestDispatcherExtension
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.data.model.StatusResponse
import com.adyen.checkout.core.error.internal.HttpError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import org.json.JSONException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class DefaultStatusRepositoryTest(
    @param:Mock private val statusService: StatusService
) {

    private lateinit var statusRepository: DefaultStatusRepository

    @Test
    fun `when receiving the final result, then it should be emitted and the flow should end`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val response = StatusResponse(resultCode = "final")
        whenever(statusService.checkStatus(any(), any())) doReturn response

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        advanceUntilIdle()

        val expected = Result.success(response)
        assertEquals(expected, statusFlow.latestValue)

        statusFlow.cancel()
    }

    @Test
    fun `when refreshing the status, then the result is emitted immediately`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val refreshResponse = StatusResponse(resultCode = "refresh")
        whenever(statusService.checkStatus(any(), any()))
            .doReturn(StatusResponse(resultCode = "pending"), refreshResponse)

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        statusRepository.refreshStatus("test")

        advanceUntilIdle()

        val expected = Result.success(refreshResponse)
        assertEquals(expected, statusFlow.latestValue)

        statusFlow.cancel()
    }

    @Test
    fun `when fetching the status multiple times at the same time, then it is only fetched once`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())) doReturn StatusResponse(resultCode = "pending")

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        statusRepository.refreshStatus("test")
        statusRepository.refreshStatus("test")
        statusRepository.refreshStatus("test")

        testScheduler.advanceTimeBy(DefaultStatusRepository.DEBOUNCE_TIME + 1)

        verify(statusService, times(1)).checkStatus(any(), any())

        statusFlow.cancel()
    }

    @Test
    fun `when polling result is final, then the flow is cancelled`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())) doReturn StatusResponse(resultCode = "authorised")

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(DefaultStatusRepository.DEBOUNCE_TIME + 1)

        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @Test
    fun `when max polling time is exceeded, then a value is emitted and the flow is cancelled`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())) doReturn StatusResponse(resultCode = "pending")

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(MAX_POLLING_DURATION + 2100)

        assertTrue(statusFlow.latestValue.isFailure)
        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @ParameterizedTest
    @MethodSource("statusFailuresSource")
    fun `when fetching the status fails, then nothing is emitted`(exception: Exception) = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())) doAnswer { throw exception }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(DefaultStatusRepository.DEBOUNCE_TIME + 1)

        verify(statusService, times(1)).checkStatus(any(), any())
        assertTrue(statusFlow.values.isEmpty())

        statusFlow.cancel()
    }

    @Test
    fun `when fetching the status fails and then returns a final result, then the final result is emitted`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val response = StatusResponse(resultCode = "authorised")
        whenever(statusService.checkStatus(any(), any()))
            .doAnswer { throw IOException() }
            .doReturn(response)

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        advanceUntilIdle()

        assertEquals(listOf(Result.success(response)), statusFlow.values)
        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @Test
    fun `when fetching the status keeps failing, then max polling time still ends the flow`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())) doAnswer { throw IOException() }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(MAX_POLLING_DURATION + 2100)

        assertEquals(1, statusFlow.values.size)
        assertInstanceOf(IllegalStateException::class.java, statusFlow.latestValue.exceptionOrNull())
        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    private fun createRepository(
        testScheduler: TestCoroutineScheduler,
        testTimeSource: TimeSource
    ): DefaultStatusRepository {
        return DefaultStatusRepository(
            statusService = statusService,
            clientKey = "someclientkey",
            timeSource = testTimeSource,
            coroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
        )
    }

    companion object {
        private val MAX_POLLING_DURATION = 1.minutes.inWholeMilliseconds

        @JvmStatic
        fun statusFailuresSource() = listOf(
            arguments(UnknownHostException()),
            arguments(SocketTimeoutException()),
            arguments(HttpError(code = 500, message = "Internal server error", errorBody = null)),
            arguments(HttpError(code = 401, message = "Unauthorized", errorBody = null)),
            arguments(JSONException("Invalid JSON")),
        )
    }
}
