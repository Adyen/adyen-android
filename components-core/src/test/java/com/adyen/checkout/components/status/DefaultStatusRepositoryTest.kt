/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/8/2022.
 */

package com.adyen.checkout.components.status

import app.cash.turbine.test
import com.adyen.checkout.components.status.api.StatusService
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultStatusRepositoryTest(
    @Mock private val statusService: StatusService
) {

    private val statusRepository = DefaultStatusRepository(statusService, "someclientkey")

    @Test
    fun `when receiving the final result, then it should be emitted and the flow should end`(
        dispatcher: TestDispatcher
    ) = runTest(dispatcher) {
        val response = StatusResponse(resultCode = "final")
        whenever(statusService.checkStatus(any(), any())) doReturn response

        statusRepository
            .poll("paymentData")
            .flowOn(dispatcher)
            .test {
                val expected = Result.success(response)
                assertEquals(expected, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `when refreshing the status, then the result is emitted immediately`(
        dispatcher: TestDispatcher
    ) = runTest(dispatcher) {
        val refreshResponse = StatusResponse(resultCode = "refresh")
        whenever(statusService.checkStatus(any(), any()))
            // return final result first, so polling stops
            .doReturn(StatusResponse(resultCode = "final"), refreshResponse)

        statusRepository
            .poll("paymentData")
            .flowOn(dispatcher)
            .test {
                skipItems(1)

                statusRepository.refreshStatus("test")

                val expected = Result.success(refreshResponse)
                assertEquals(expected, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
    }
}
