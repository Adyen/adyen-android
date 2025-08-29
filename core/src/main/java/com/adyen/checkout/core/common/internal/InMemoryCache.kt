/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/8/2025.
 */

package com.adyen.checkout.core.common.internal

import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache

internal class InMemoryCache(
    maxSize: Int,
) {

    private val cache = object : LruCache<String, Value>(maxSize) {
        override fun sizeOf(key: String, value: Value) = value.size
    }

    operator fun get(key: String): Bitmap? {
        return cache.get(key)?.bitmap
    }

    @Suppress("DEPRECATION", "MagicNumber")
    operator fun set(key: String, bitmap: Bitmap) {
        val size = try {
            bitmap.allocationByteCount
        } catch (_: Exception) {
            val bytesPerPixel = when {
                bitmap.config == Bitmap.Config.ALPHA_8 -> 1
                bitmap.config == Bitmap.Config.RGB_565 -> 2
                bitmap.config == Bitmap.Config.ARGB_4444 -> 2
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.RGBA_F16 -> 8
                else -> 4
            }
            bitmap.width * bitmap.height * bytesPerPixel
        }

        cache.put(key, Value(bitmap, size))
    }

    private class Value(
        val bitmap: Bitmap,
        val size: Int,
    )
}
