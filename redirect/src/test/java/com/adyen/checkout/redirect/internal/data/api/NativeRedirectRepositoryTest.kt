/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2026.
 */

package com.adyen.checkout.redirect.internal.data.api

import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.UnknownHostException

@ExtendWith(MockitoExtension::class)
internal class NativeRedirectRepositoryTest(
    @param:Mock private val nativeRedirectService: NativeRedirectService,
) {

    private lateinit var repository: NativeRedirectRepository

    @BeforeEach
    fun setup() {
        repository = NativeRedirectRepository(nativeRedirectService)
    }

    @Test
    fun `when makeNativeRedirect is called, then the request is sent with the given data`() = runTest {
        // GIVEN
        whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn
            NativeRedirectResponse("redirectResult")

        // WHEN
        repository.makeNativeRedirect(
            nativeRedirectData = "nativeRedirectData",
            returnQueryString = "returnQueryString",
            clientKey = TEST_CLIENT_KEY,
        )

        // THEN
        val expectedRequest = NativeRedirectRequest(
            redirectData = "nativeRedirectData",
            returnQueryString = "returnQueryString",
        )
        verify(nativeRedirectService).makeNativeRedirect(expectedRequest, TEST_CLIENT_KEY)
    }

    @Test
    fun `when the service succeeds, then the serialized response is returned`() = runTest {
        // GIVEN
        val response = NativeRedirectResponse("redirectResult")
        whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doReturn response

        // WHEN
        val result = repository.makeNativeRedirect("nativeRedirectData", "returnQueryString", TEST_CLIENT_KEY)

        // THEN
        val expected = NativeRedirectResponse.SERIALIZER.serialize(response)
        assertEquals(expected.toString(), result.getOrThrow().toString())
    }

    @Test
    fun `when the service throws an HttpError, then a failure with that error is returned`() = runTest {
        // GIVEN
        val error = HttpError(401, "Unauthorized", null)
        whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }

        // WHEN
        val result = repository.makeNativeRedirect("nativeRedirectData", "returnQueryString", TEST_CLIENT_KEY)

        // THEN
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `when the service throws an IOException, then a failure with that exception is returned`() = runTest {
        // GIVEN
        val error = UnknownHostException("Unable to resolve host")
        whenever(nativeRedirectService.makeNativeRedirect(any(), any())) doAnswer { throw error }

        // WHEN
        val result = repository.makeNativeRedirect("nativeRedirectData", "returnQueryString", TEST_CLIENT_KEY)

        // THEN
        assertEquals(error, result.exceptionOrNull())
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
    }
}
