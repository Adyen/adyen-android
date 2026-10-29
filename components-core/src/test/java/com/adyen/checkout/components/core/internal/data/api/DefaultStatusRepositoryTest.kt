/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/8/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class DefaultStatusRepositoryTest(
    @Mock private val statusService: StatusService
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

    @Test
    fun `when status checks fail with UnknownHostException until max duration, then the timeout is emitted`() =
        runTest {
            statusRepository = createRepository(testScheduler, testTimeSource)
            whenever(statusService.checkStatus(any(), any())).thenAnswer {
                throw UnknownHostException("Unable to resolve host")
            }

            val statusFlow = statusRepository
                .poll("paymentData", MAX_POLLING_DURATION)
                .test(testScheduler)

            testScheduler.advanceTimeBy(MAX_POLLING_DURATION)

            assertTrue(statusFlow.values.isEmpty())

            testScheduler.advanceTimeBy(POLLING_INTERVAL_AFTER_CAP)

            val failure = statusFlow.latestValue.exceptionOrNull()
            assertEquals(IllegalStateException::class.java, failure?.javaClass)
            assertEquals(DefaultStatusRepository.STATUS_POLLING_TIMEOUT_MESSAGE, failure?.message)
            assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

            statusFlow.cancel()
        }

    @Test
    fun `when an UnknownHostException is followed by a final status, then that status is emitted`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val response = StatusResponse(resultCode = "authorised")
        var invocations = 0
        whenever(statusService.checkStatus(any(), any())).thenAnswer {
            if (invocations++ == 0) {
                throw UnknownHostException("Unable to resolve host")
            } else {
                response
            }
        }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(RECOVERY_POLL_WINDOW)

        assertEquals(Result.success(response), statusFlow.latestValue)
        assertTrue(statusFlow.values.none { it.isFailure })
        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @Test
    fun `when a SocketTimeoutException is followed by a final status, then both are emitted`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val response = StatusResponse(resultCode = "authorised")
        var invocations = 0
        whenever(statusService.checkStatus(any(), any())).thenAnswer {
            if (invocations++ == 0) {
                throw SocketTimeoutException("read timed out")
            } else {
                response
            }
        }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(RECOVERY_POLL_WINDOW)

        assertEquals(SocketTimeoutException::class.java, statusFlow.values[0].exceptionOrNull()?.javaClass)
        assertEquals(Result.success(response), statusFlow.latestValue)
        assertInstanceOf(CancellationException::class.java, statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @Test
    fun `when a status check fails with HttpException, then the failure is emitted`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        val error = HttpException(503, "unavailable", null)
        whenever(statusService.checkStatus(any(), any())).thenAnswer { throw error }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(DefaultStatusRepository.DEBOUNCE_TIME + 1)

        assertEquals(error, statusFlow.latestValue.exceptionOrNull())
        assertNull(statusFlow.completionThrowable)

        statusFlow.cancel()
    }

    @Test
    fun `when a status check is cancelled, then the cancellation is not emitted as a failure`() = runTest {
        statusRepository = createRepository(testScheduler, testTimeSource)
        whenever(statusService.checkStatus(any(), any())).thenAnswer {
            throw CancellationException("cancelled")
        }

        val statusFlow = statusRepository
            .poll("paymentData", MAX_POLLING_DURATION)
            .test(testScheduler)

        testScheduler.advanceTimeBy(DefaultStatusRepository.DEBOUNCE_TIME + 1)

        assertTrue(statusFlow.values.isEmpty())
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
        private const val POLLING_INTERVAL_AFTER_CAP = 2_100L
        private const val RECOVERY_POLL_WINDOW = 3_000L
    }
}
