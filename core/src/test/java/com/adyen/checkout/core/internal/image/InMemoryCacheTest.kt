/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/8/2026.
 */

package com.adyen.checkout.core.internal.image

import android.graphics.Bitmap
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

internal class InMemoryCacheTest {

    @Test
    fun `when a bitmap is stored, then it can be retrieved`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val bitmap = bitmapOfSize(BITMAP_SIZE)

        // WHEN
        cache[KEY] = bitmap

        // THEN
        assertSame(bitmap, cache[KEY])
    }

    @Test
    fun `when a key is not stored, then null is returned`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)

        // THEN
        assertNull(cache[KEY])
    }

    @Test
    fun `when a key is stored twice, then the latest bitmap is returned`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val first = bitmapOfSize(BITMAP_SIZE)
        val second = bitmapOfSize(BITMAP_SIZE)

        // WHEN
        cache[KEY] = first
        cache[KEY] = second

        // THEN
        assertNotSame(first, cache[KEY])
        assertSame(second, cache[KEY])
    }

    @Test
    fun `when the maximum size is exceeded, then the least recently used bitmap is evicted`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val first = bitmapOfSize(MAX_SIZE / 2 + 1)
        val second = bitmapOfSize(MAX_SIZE / 2 + 1)

        // WHEN
        cache[KEY] = first
        cache[OTHER_KEY] = second

        // THEN
        assertNull(cache[KEY])
        assertSame(second, cache[OTHER_KEY])
    }

    @Test
    fun `when a bitmap is read, then it is not the first one to be evicted`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val first = bitmapOfSize(MAX_SIZE / 2)
        val second = bitmapOfSize(MAX_SIZE / 2)
        val third = bitmapOfSize(MAX_SIZE / 2)
        cache[KEY] = first
        cache[OTHER_KEY] = second

        // WHEN
        // Reading the first entry makes the second one the least recently used.
        cache[KEY]
        cache[THIRD_KEY] = third

        // THEN
        assertSame(first, cache[KEY])
        assertNull(cache[OTHER_KEY])
        assertSame(third, cache[THIRD_KEY])
    }

    @Test
    fun `when a bitmap does not fit in the cache, then it is not retained`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val bitmap = bitmapOfSize(MAX_SIZE + 1)

        // WHEN
        cache[KEY] = bitmap

        // THEN
        assertNull(cache[KEY])
    }

    @Test
    fun `when the allocation byte count is unavailable, then the size is based on the dimensions`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        // The exact byte size is not asserted, because the Bitmap.Config constants are not populated in
        // unit tests, which makes the bytes per pixel of the fallback environment dependent.
        val bitmap = mock<Bitmap> {
            on { allocationByteCount } doThrow RuntimeException("Not available")
            on { width } doReturn DIMENSION
            on { height } doReturn DIMENSION
        }

        // WHEN
        cache[KEY] = bitmap

        // THEN
        assertNotNull(cache[KEY])
    }

    @Test
    fun `when the fallback size does not fit in the cache, then it is not retained`() {
        // GIVEN
        val cache = InMemoryCache(MAX_SIZE)
        val bitmap = mock<Bitmap> {
            on { allocationByteCount } doThrow RuntimeException("Not available")
            on { width } doReturn MAX_SIZE
            on { height } doReturn MAX_SIZE
        }

        // WHEN
        cache[KEY] = bitmap

        // THEN
        assertNull(cache[KEY])
    }

    private fun bitmapOfSize(size: Int): Bitmap = mock {
        on { allocationByteCount } doReturn size
    }

    companion object {

        private const val KEY = "https://adyen.com/visa.png"
        private const val OTHER_KEY = "https://adyen.com/mc.png"
        private const val THIRD_KEY = "https://adyen.com/amex.png"

        private const val MAX_SIZE = 1000
        private const val BITMAP_SIZE = 100
        private const val DIMENSION = 10
    }
}
