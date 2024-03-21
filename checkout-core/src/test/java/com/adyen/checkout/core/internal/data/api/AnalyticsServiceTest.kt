/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */

package com.adyen.checkout.core.internal.data.api

import com.adyen.checkout.core.internal.data.model.AnalyticsTrackRequest
import com.adyen.checkout.core.internal.data.model.EmptyResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class AnalyticsServiceTest(
    @Mock private val httpClient: HttpClient
) {

    private lateinit var analyticsService: AnalyticsService

    @BeforeEach
    fun setup() {
        analyticsService = AnalyticsService(
            httpClient = httpClient,
        )
    }

    @Test
    fun `when sending events, then an empty response is expected`() = runTest {
        val request = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = emptyList(),
            logs = emptyList(),
        )
        val checkoutAttemptId = "testtest"
        whenever(
            httpClient.post(
                org.mockito.kotlin.eq("v3/analytics/$checkoutAttemptId"),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            ),
        )
            .doReturn(ByteArray(0))

        val response = analyticsService.sendEvents(request, checkoutAttemptId, TEST_CLIENT_KEY)

        Assertions.assertInstanceOf(EmptyResponse::class.java, response)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
