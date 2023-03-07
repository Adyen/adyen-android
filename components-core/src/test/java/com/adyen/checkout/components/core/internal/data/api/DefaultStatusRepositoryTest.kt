/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/8/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import app.cash.turbine.test
import com.adyen.checkout.components.core.internal.data.model.StatusResponse
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultStatusRepositoryTest(
    @Mock private val statusService: StatusService
) {

    private lateinit var statusRepository: DefaultStatusRepository

    @BeforeEach
    fun beforeEach() {
        statusRepository = DefaultStatusRepository(statusService, "someclientkey")
    }

    @Test
    fun `when receiving the final result, then it should be emitted and the flow should end`() = runTest {
        val response = StatusResponse(resultCode = "final")
        whenever(statusService.checkStatus(any(), any())) doReturn response

        statusRepository
            .poll("paymentData", DEFAULT_MAX_POLLING_DURATION)
            .test {
                val expected = Result.success(response)
                assertEquals(expected, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `when refreshing the status, then the result is emitted immediately`() = runTest() {
        val refreshResponse = StatusResponse(resultCode = "refresh")
        whenever(statusService.checkStatus(any(), any()))
            // return final result first, so polling stops
            .doReturn(StatusResponse(resultCode = "final"), refreshResponse)

        statusRepository
            .poll("paymentData", DEFAULT_MAX_POLLING_DURATION)
            .test {
                skipItems(1)

                statusRepository.refreshStatus("test")

                val expected = Result.success(refreshResponse)
                assertEquals(expected, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
    }

    companion object {
        private val DEFAULT_MAX_POLLING_DURATION = TimeUnit.MINUTES.toMillis(10)
    }
}
