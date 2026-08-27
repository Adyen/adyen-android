/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/8/2026.
 */

package com.adyen.checkout.core.internal.image

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.components.internal.ApplicationContextHolder
import com.adyen.checkout.core.error.internal.HttpError
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object DefaultImageLoader : ImageLoader {

    private const val LOW_MEMORY_PERCENT = 0.15
    private const val DEFAULT_MEMORY_PERCENT = 0.2
    private const val DEFAULT_MEMORY_MEGABYTES = 256
    private const val BYTE_CONVERSION = 1024

    private val okHttpClient = OkHttpClient()

    private val cache = InMemoryCache(calculateInMemoryCacheSize(ApplicationContextHolder.require()))

    private fun calculateInMemoryCacheSize(context: Context): Int = try {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val percent = if (activityManager.isLowRamDevice) LOW_MEMORY_PERCENT else DEFAULT_MEMORY_PERCENT
        val isLargeHeap = (context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP) != 0
        val memoryMegabytes = if (isLargeHeap) activityManager.largeMemoryClass else activityManager.memoryClass
        // Available megabytes to kilobytes to bytes
        (percent * memoryMegabytes * BYTE_CONVERSION * BYTE_CONVERSION).toInt()
    } catch (_: Exception) {
        (DEFAULT_MEMORY_PERCENT * DEFAULT_MEMORY_MEGABYTES * BYTE_CONVERSION * BYTE_CONVERSION).toInt()
    }

    override suspend fun load(url: String): Result<Bitmap> {
        val cachedBitmap = cache[url]
        if (cachedBitmap != null) {
            return Result.success(cachedBitmap)
        }

        return withContext(DispatcherProvider.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).await().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes() ?: ByteArray(0)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    if (bitmap != null) {
                        cache[url] = bitmap
                        Result.success(bitmap)
                    } else {
                        Result.failure(IOException("Failed to decode bitmap."))
                    }
                } else {
                    Result.failure(HttpError(response.code, response.message, null))
                }
            }
        }
    }

    private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) = continuation.resume(response)
                override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)
            },
        )
    }
}
