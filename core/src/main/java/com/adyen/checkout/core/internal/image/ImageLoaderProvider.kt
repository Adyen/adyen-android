/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/8/2026.
 */

package com.adyen.checkout.core.internal.image

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.collection.LruCache
import com.adyen.checkout.core.components.internal.ApplicationContextHolder

/**
 * Holds the process wide [DefaultImageLoader], so that the in memory cache is shared and used optimally.
 *
 * The instance is created lazily, which makes sure the application context is only required when an image is actually
 * loaded.
 */
internal object ImageLoaderProvider {

    val instance: DefaultImageLoader by lazy {
        val cacheSize = calculateInMemoryCacheSize(ApplicationContextHolder.require())
        DefaultImageLoader(
            cache = InMemoryCache(cacheSize),
            failureCache = LruCache(FAILURE_CACHE_SIZE),
        )
    }

    private const val LOW_MEMORY_PERCENT = 0.15
    private const val DEFAULT_MEMORY_PERCENT = 0.2
    private const val DEFAULT_MEMORY_MEGABYTES = 256
    private const val BYTE_CONVERSION = 1024
    private const val FAILURE_CACHE_SIZE = 64

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
