/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/10/2026.
 */

package com.adyen.checkout.ui.core.old.internal.util

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.view.View
import androidx.core.content.ContextCompat
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.AdyenLogger
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.ui.ImageLoader
import com.adyen.checkout.core.old.internal.ui.PermissionHandler
import com.adyen.checkout.ui.core.old.internal.ui.imageLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.MockedStatic
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class ImageSaverTest {

    @TempDir
    lateinit var tempDir: File

    private val context = mock<Context>()
    private val permissionHandler = mock<PermissionHandler>()
    private val bitmap = mock<Bitmap>()

    private lateinit var imageLoadingExtensions: MockedStatic<*>

    @BeforeEach
    fun setup() {
        AdyenLogger.setLogLevel(AdyenLogLevel.NONE)
        imageLoadingExtensions = mockStatic(Class.forName(IMAGE_LOADING_EXTENSIONS_CLASS))
    }

    @AfterEach
    fun tearDown() {
        imageLoadingExtensions.close()
    }

    @Test
    fun `when image loader returns a bitmap, then the bitmap is saved and the result is success`() = runTest {
        // GIVEN
        val imageLoader = TestImageLoader(bitmap = bitmap)
        givenImageLoader(imageLoader)
        val imageSaver = createImageSaver()

        // WHEN
        val result = withGrantedStorage {
            imageSaver.saveImageFromUrl(context, permissionHandler, IMAGE_URL, FILE_NAME, FILE_RELATIVE_PATH)
        }

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(IMAGE_URL, imageLoader.requestedUrl)
        verify(bitmap).compress(eq(Bitmap.CompressFormat.PNG), eq(100), any())
        assertTrue(File(tempDir, FILE_NAME).exists())
    }

    @Test
    fun `when image loader returns a bitmap, then the bitmap is not recycled`() = runTest {
        // GIVEN
        givenImageLoader(TestImageLoader(bitmap = bitmap))
        val imageSaver = createImageSaver()

        // WHEN
        val result = withGrantedStorage {
            imageSaver.saveImageFromUrl(context, permissionHandler, IMAGE_URL, FILE_NAME, FILE_RELATIVE_PATH)
        }

        // THEN
        assertTrue(result.isSuccess)
        verify(bitmap, never()).recycle()
    }

    @Test
    fun `when image loader returns an error, then the result is failure`() = runTest {
        // GIVEN
        val error = IOException("Network error")
        givenImageLoader(TestImageLoader(error = error))
        val imageSaver = createImageSaver()

        // WHEN
        val result = imageSaver.saveImageFromUrl(context, permissionHandler, IMAGE_URL)

        // THEN
        val exception = result.exceptionOrNull()
        assertTrue(exception is CheckoutException)
        assertEquals(error, exception?.cause)
    }

    @Test
    fun `when image loader throws an exception, then the result is failure`() = runTest {
        // GIVEN
        val error = IllegalArgumentException("Unexpected URL")
        givenImageLoader(TestImageLoader(exceptionToThrow = error))
        val imageSaver = createImageSaver()

        // WHEN
        val result = imageSaver.saveImageFromUrl(context, permissionHandler, IMAGE_URL)

        // THEN
        val exception = result.exceptionOrNull()
        assertTrue(exception is CheckoutException)
        assertEquals(error, exception?.cause)
    }

    @Test
    fun `when image loader completes without a callback, then the result is failure`() = runTest {
        // GIVEN
        givenImageLoader(TestImageLoader())
        val imageSaver = createImageSaver()

        // WHEN
        val result = imageSaver.saveImageFromUrl(context, permissionHandler, IMAGE_URL)

        // THEN
        assertTrue(result.exceptionOrNull() is CheckoutException)
    }

    @Test
    fun `when image url is malformed, then the result is failure and the image is not loaded`() = runTest {
        // GIVEN
        val imageLoader = TestImageLoader(bitmap = bitmap)
        givenImageLoader(imageLoader)
        val imageSaver = createImageSaver()

        // WHEN
        val result = imageSaver.saveImageFromUrl(context, permissionHandler, "")

        // THEN
        assertTrue(result.exceptionOrNull() is CheckoutException)
        assertNull(imageLoader.requestedUrl)
    }

    @Test
    fun `when saving an image from a view, then the created bitmap is recycled`() = runTest {
        // GIVEN
        val imageSaver = createImageSaver()
        val view = mock<View> {
            on { width } doReturn 1
            on { height } doReturn 1
        }

        // WHEN
        val result = mockStatic(Bitmap::class.java).use {
            whenever(Bitmap.createBitmap(any<Int>(), any<Int>(), any<Bitmap.Config>())).thenReturn(bitmap)
            mockConstruction(Canvas::class.java).use {
                withGrantedStorage {
                    imageSaver.saveImageFromView(
                        context = context,
                        permissionHandler = permissionHandler,
                        view = view,
                        backgroundColor = Color.WHITE,
                        fileName = FILE_NAME,
                        fileRelativePath = FILE_RELATIVE_PATH,
                    )
                }
            }
        }

        // THEN
        assertTrue(result.isSuccess)
        verify(bitmap).recycle()
    }

    // Mockito static mocks are thread-local, so ImageSaver must run on the test thread.
    private fun TestScope.createImageSaver() = ImageSaver(dispatcher = UnconfinedTestDispatcher(testScheduler))

    private fun givenImageLoader(imageLoader: ImageLoader) {
        whenever(context.imageLoader).thenReturn(imageLoader)
    }

    // Runs the API 28 and below path, where the image is written to a public directory after a permission check.
    private suspend fun <T> withGrantedStorage(block: suspend () -> T): T {
        mockStatic(ContextCompat::class.java).use {
            whenever(ContextCompat.checkSelfPermission(any(), any())).thenReturn(PackageManager.PERMISSION_GRANTED)
            mockStatic(Environment::class.java).use {
                whenever(Environment.getExternalStoragePublicDirectory(any())).thenReturn(tempDir)
                mockConstruction(ContentValues::class.java) { contentValues, _ ->
                    whenever(contentValues.getAsString(Media.DISPLAY_NAME)).thenReturn(FILE_NAME)
                    whenever(contentValues.getAsString(Media.RELATIVE_PATH)).thenReturn(FILE_RELATIVE_PATH)
                }.use {
                    return block()
                }
            }
        }
    }

    private class TestImageLoader(
        private val bitmap: Bitmap? = null,
        private val error: Throwable? = null,
        private val exceptionToThrow: Exception? = null,
    ) : ImageLoader {

        var requestedUrl: String? = null
            private set

        override suspend fun load(
            url: String,
            onSuccess: suspend (Bitmap) -> Unit,
            onError: suspend (Throwable) -> Unit
        ) {
            requestedUrl = url
            exceptionToThrow?.let { throw it }
            bitmap?.let { onSuccess(it) }
            error?.let { onError(it) }
        }
    }

    companion object {
        private const val IMAGE_URL = "https://checkoutshopper-test.adyen.com/qr-code.png"
        private const val FILE_NAME = "qr-code"
        private const val FILE_RELATIVE_PATH = "Download"
        private const val IMAGE_LOADING_EXTENSIONS_CLASS =
            "com.adyen.checkout.ui.core.old.internal.ui.ImageLoadingExtensionsKt"
    }
}
