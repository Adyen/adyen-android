/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.core.image

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.api.HttpException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ImageLoader {

    suspend fun load(
        url: String,
        onSuccess: suspend (Bitmap) -> Unit,
        onError: suspend (Throwable) -> Unit
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultImageLoader(context: Context) : ImageLoader {

    private val okHttpClient = OkHttpClient()

    private val cache = InMemoryCache(calculateInMemoryCacheSize(context))

    override suspend fun load(
        url: String,
        onSuccess: suspend (Bitmap) -> Unit,
        onError: suspend (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val cachedBitmap = cache[url]
        if (cachedBitmap != null) {
            withContext(Dispatchers.Main) {
                onSuccess(cachedBitmap)
            }
            return@withContext
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val call = okHttpClient.newCall(request)

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                val bytes = response.body
                    ?.bytes()
                    ?: ByteArray(0)

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                cache[url] = bitmap

                withContext(Dispatchers.Main) {
                    onSuccess(bitmap)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError(HttpException(response.code, response.message, null))
                }
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
        }
    }

    companion object {

        private const val LOW_MEMORY_PERCENT = 0.15
        private const val DEFAULT_MEMORY_PERCENT = 0.2
        private const val DEFAULT_MEMORY_MEGABYTES = 256
        private const val BYTE_CONVERSION = 1024

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
    }
}
