/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/8/2026.
 */

package com.adyen.checkout.core.internal.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import com.adyen.checkout.core.error.internal.HttpError
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

internal class DefaultImageLoaderTest {

    private val testDispatcher = StandardTestDispatcher()

    private val interceptor = FakeInterceptor()

    private val bitmap = mock<Bitmap>()

    private lateinit var bitmapFactory: MockedStatic<BitmapFactory>

    private lateinit var imageLoader: DefaultImageLoader

    @BeforeEach
    fun before() {
        // BitmapFactory is a framework class, so decoding is stubbed instead of pulling in Robolectric.
        bitmapFactory = mockStatic(BitmapFactory::class.java)
        decodesTo(bitmap)

        imageLoader = DefaultImageLoader(
            cache = InMemoryCache(CACHE_SIZE),
            failureCache = LruCache(FAILURE_CACHE_SIZE),
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                // Run calls on the calling thread, so enqueue() stays deterministic under the test dispatcher.
                .dispatcher(Dispatcher(DirectExecutorService()))
                .build(),
            dispatcher = testDispatcher,
        )
    }

    @AfterEach
    fun after() {
        bitmapFactory.close()
    }

    private fun decodesTo(bitmap: Bitmap?) {
        whenever(BitmapFactory.decodeByteArray(any(), any(), any())).thenReturn(bitmap)
    }

    @Test
    fun `when the response is successful, then a bitmap is returned`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }

        // WHEN
        val result = imageLoader.load(URL)

        // THEN
        assertTrue(result.isSuccess)
        assertSame(bitmap, result.getOrNull())
    }

    @Test
    fun `when the response body cannot be decoded, then a failure is returned`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }
        decodesTo(null)

        // WHEN
        val result = imageLoader.load(URL)

        // THEN
        assertTrue(result.isFailure)
        assertInstanceOf<IOException>(result.exceptionOrNull())
    }

    @Test
    fun `when the same url is loaded twice, then it is only requested once`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }

        // WHEN
        imageLoader.load(URL)
        val result = imageLoader.load(URL)

        // THEN
        assertSame(bitmap, result.getOrNull())
        assertEquals(1, interceptor.requestCount)
    }

    @Test
    fun `when different urls are loaded, then each one is requested`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }

        // WHEN
        imageLoader.load(URL)
        imageLoader.load(OTHER_URL)

        // THEN
        assertEquals(2, interceptor.requestCount)
    }

    @Test
    fun `when the response is a client error, then a http error is returned`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.error(HTTP_NOT_FOUND, "Not Found") }

        // WHEN
        val result = imageLoader.load(URL)

        // THEN
        assertTrue(result.isFailure)
        val error = assertInstanceOf<HttpError>(result.exceptionOrNull())
        assertEquals(HTTP_NOT_FOUND, error.code)
    }

    @Test
    fun `when the response is a client error, then the failure is cached`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.error(HTTP_NOT_FOUND, "Not Found") }

        // WHEN
        val first = imageLoader.load(URL)
        val second = imageLoader.load(URL)

        // THEN
        // The exact same error instance is returned, which can only come from the failure cache.
        assertSame(first.exceptionOrNull(), second.exceptionOrNull())
        assertEquals(HTTP_NOT_FOUND, assertInstanceOf<HttpError>(second.exceptionOrNull()).code)
        assertEquals(1, interceptor.requestCount)
    }

    @Test
    fun `when the response is a server error, then the failure is not cached`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.error(HTTP_SERVER_ERROR, "Internal Server Error") }

        // WHEN
        imageLoader.load(URL)
        val result = imageLoader.load(URL)

        // THEN
        assertTrue(result.isFailure)
        assertEquals(2, interceptor.requestCount)
    }

    @Test
    fun `when the request fails with an io exception, then a failure is returned`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { throw IOException("No connection") }

        // WHEN
        val result = imageLoader.load(URL)

        // THEN
        assertTrue(result.isFailure)
        assertInstanceOf<IOException>(result.exceptionOrNull())
    }

    @Test
    fun `when the request fails with an io exception, then the next load is retried`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { throw IOException("No connection") }

        // WHEN
        imageLoader.load(URL)
        imageLoader.load(URL)

        // THEN
        assertEquals(2, interceptor.requestCount)
    }

    @Test
    fun `when a failed load succeeds on retry, then a bitmap is returned`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { throw IOException("No connection") }
        imageLoader.load(URL)

        // WHEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }
        val result = imageLoader.load(URL)

        // THEN
        assertSame(bitmap, result.getOrNull())
    }

    @Test
    fun `when the same url is loaded concurrently, then it is only requested once`() = runTest(testDispatcher) {
        // GIVEN
        interceptor.respondWith { it.success(IMAGE_BYTES) }

        // WHEN
        val first = async { imageLoader.load(URL) }
        val second = async { imageLoader.load(URL) }

        // THEN
        assertTrue(first.await().isSuccess)
        assertTrue(second.await().isSuccess)
        assertEquals(1, interceptor.requestCount)
    }

    private class FakeInterceptor : Interceptor {

        var requestCount = 0
            private set

        private var responder: (Request) -> Response = { it.success(IMAGE_BYTES) }

        fun respondWith(responder: (Request) -> Response) {
            this.responder = responder
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            requestCount++
            return responder(chain.request())
        }
    }

    /**
     * Executes every task on the calling thread, so OkHttp's asynchronous calls complete synchronously.
     */
    private class DirectExecutorService : AbstractExecutorService() {

        override fun execute(command: Runnable) = command.run()

        override fun shutdown() = Unit

        override fun shutdownNow(): List<Runnable> = emptyList()

        override fun isShutdown() = false

        override fun isTerminated() = false

        override fun awaitTermination(timeout: Long, unit: TimeUnit) = true
    }

    companion object {

        private const val URL = "https://adyen.com/visa.png"
        private const val OTHER_URL = "https://adyen.com/mc.png"

        private const val HTTP_OK = 200
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500

        private const val CACHE_SIZE = 1024
        private const val FAILURE_CACHE_SIZE = 64

        // The contents do not matter, because decoding is stubbed.
        private val IMAGE_BYTES = byteArrayOf(1, 2, 3)

        private fun Request.success(body: ByteArray) = buildResponse(HTTP_OK, "OK", body)

        private fun Request.error(code: Int, message: String) = buildResponse(code, message, ByteArray(0))

        private fun Request.buildResponse(code: Int, message: String, body: ByteArray) = Response.Builder()
            .request(this)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(body.toResponseBody("image/png".toMediaType()))
            .build()
    }
}
