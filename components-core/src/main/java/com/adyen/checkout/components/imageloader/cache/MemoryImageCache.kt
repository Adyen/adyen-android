/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 5/12/2022.
 */

package com.adyen.checkout.components.imageloader.cache

import android.graphics.Bitmap
import android.util.LruCache

class MemoryImageCache : ImageCache {

    private val cache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(LRU_CACHE_MAX_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            // The cache size will be measured in kilobytes rather than number of items.
            return value.byteCount / KILO_BYTE_SIZE
        }
    }

    override fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    override fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    override fun clear() {
        cache.evictAll()
    }

    companion object {
        private const val CACHE_FRACTION_SIZE = 8
        private const val KILO_BYTE_SIZE = 1024
        private val LRU_CACHE_MAX_SIZE = getMaxCacheSize()

        private fun getMaxCacheSize(): Int {
            val availableMemory = (Runtime.getRuntime().maxMemory() / KILO_BYTE_SIZE).toInt()
            return availableMemory / CACHE_FRACTION_SIZE
        }
    }
}
