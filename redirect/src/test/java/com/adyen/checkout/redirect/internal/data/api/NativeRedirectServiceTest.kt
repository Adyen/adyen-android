package com.adyen.checkout.redirect.internal.data.api

import com.adyen.checkout.core.old.internal.data.api.HttpClient
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
internal class NativeRedirectServiceTest(
    @Mock private val httpClient: HttpClient
) {

    private lateinit var nativeRedirectService: NativeRedirectService

    @BeforeEach
    fun setup() {
        nativeRedirectService = NativeRedirectService(
            httpClient = httpClient,
        )
    }

    @Test
    fun `makeNativeRedirect follows contract`() = runTest {
        val request = NativeRedirectRequest(
            redirectData = "redirectData",
            returnQueryString = "returnQueryString",
        )
        val clientKey = "clientKey"

        // Ignore any error since we only want to check if the call to post is made correctly.
        runCatching {
            nativeRedirectService.makeNativeRedirect(request, clientKey)
        }

        verify(httpClient).post(
            path = "v1/nativeRedirect/redirectResult",
            queryParameters = mapOf("clientKey" to clientKey),
            jsonBody = NativeRedirectRequest.SERIALIZER.serialize(request).toString(),
            headers = emptyMap(),
        )
    }
}
