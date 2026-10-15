/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.api

import com.adyen.checkout.core.common.internal.api.AdyenApiResponse
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.dropin.internal.data.model.DisableTokenRequest
import com.adyen.checkout.dropin.internal.data.model.DisableTokenResponse
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class DisableTokenServiceTest(
    @param:Mock private val httpClient: HttpClient,
) {

    @Test
    fun `when disabling a token, then the disable token endpoint is called with the request`() = runTest {
        whenever(httpClient.post(any(), any(), any(), any())) doReturn createApiResponse("{}")
        val service = DisableTokenService(httpClient, UnconfinedTestDispatcher(testScheduler))

        service.disableToken(
            request = DisableTokenRequest(sessionData = "session-data", storedPaymentMethodId = "spm-id"),
            sessionId = "session-id",
            clientKey = TEST_CLIENT_KEY,
        )

        val capturedBody = argumentCaptor<String>()
        verify(httpClient).post(
            eq("v1/sessions/session-id/disableToken"),
            capturedBody.capture(),
            eq(mapOf("clientKey" to TEST_CLIENT_KEY)),
            any(),
        )
        val expectedBody = JSONObject()
            .put("sessionData", "session-data")
            .put("storedPaymentMethodId", "spm-id")
        assertEquals(expectedBody.toString(), capturedBody.firstValue)
    }

    @Test
    fun `when the token is disabled, then the response is returned`() = runTest {
        whenever(httpClient.post(any(), any(), any(), any())) doReturn
            createApiResponse("""{"sessionData":"updated-session-data"}""")
        val service = DisableTokenService(httpClient, UnconfinedTestDispatcher(testScheduler))

        val response = service.disableToken(
            request = DisableTokenRequest(sessionData = "session-data", storedPaymentMethodId = "spm-id"),
            sessionId = "session-id",
            clientKey = TEST_CLIENT_KEY,
        )

        assertEquals(DisableTokenResponse(sessionData = "updated-session-data"), response)
    }

    private fun createApiResponse(body: String) = AdyenApiResponse(
        path = "v1/sessions/session-id/disableToken",
        statusCode = 200,
        headers = emptyMap(),
        body = body,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
