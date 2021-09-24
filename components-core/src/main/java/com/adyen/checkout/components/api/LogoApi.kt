/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/3/2019.
 */
package com.adyen.checkout.components.api

import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.util.LruCache
import com.adyen.checkout.components.api.LogoConnectionTask.LogoCallback
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.util.*

class LogoApi(host: String, displayMetrics: DisplayMetrics) {
    companion object {
        private val TAG = LogUtil.getTag()

        // %1$s = size, %2$s = txVariant(/txSubVariant)-densityExtension
        private const val LOGO_PATH = "images/logos/%1\$s/%2\$s.png"
        private val DEFAULT_SIZE = Size.SMALL
        const val KILO_BYTE_SIZE = 1024
        private const val CACHE_FRACTION_SIZE = 8
        private val LRU_CACHE_MAX_SIZE = getMaxCacheSize()
        private var sInstance: LogoApi? = null

        /**
         * Get the instance of the [LogoApi] for the specified environment.
         *
         * @param environment The URL of the server for fetching the images. For optimization it should be the closest to the shopper.
         * @param displayMetrics The [DisplayMetrics] of the device to fetch the correct size images.
         * @return The instance of the [LogoApi].
         */
        @JvmStatic
        fun getInstance(environment: Environment, displayMetrics: DisplayMetrics): LogoApi {
            val hostUrl = environment.baseUrl
            synchronized(LogoApi::class.java) {
                val currentInstance = sInstance
                if (currentInstance == null || currentInstance.isDifferentHost(hostUrl)) {
                    currentInstance?.clearCache()
                    val newInstance = LogoApi(hostUrl, displayMetrics)
                    sInstance = newInstance
                    return newInstance
                }
                return currentInstance
            }
        }

        private fun getMaxCacheSize(): Int {
            val availableMemory = (Runtime.getRuntime().maxMemory() / KILO_BYTE_SIZE).toInt()
            return availableMemory / CACHE_FRACTION_SIZE
        }
    }

    private val connectionsMap: MutableMap<String, LogoConnectionTask> = HashMap()
    private val logoUrlFormat: String = host + LOGO_PATH
    private val densityExtension: String = getDensityExtension(displayMetrics.densityDpi)
    private val cache: LruCache<String, BitmapDrawable> = object : LruCache<String, BitmapDrawable>(LRU_CACHE_MAX_SIZE) {
        override fun sizeOf(key: String, drawable: BitmapDrawable): Int {
            // The cache size will be measured in kilobytes rather than number of items.
            return drawable.bitmap.byteCount / KILO_BYTE_SIZE
        }
    }

    /**
     * Starts a request to get the [Drawable] of a Logo from the web.
     *
     * @param txVariant The identifier of the transaction variant.
     * @param txSubVariant The identifier of the transaction sub variant.
     * @param size The size if the desired logo;
     * @param callback The callback for when the request is completed.
     */
    fun getLogo(
        txVariant: String,
        txSubVariant: String?,
        size: Size?,
        callback: LogoCallback
    ) {
        Logger.v(TAG, "getLogo - $txVariant, $txSubVariant, $size")
        val logoUrl = buildUrl(txVariant, txSubVariant, size)
        synchronized(this) {
            val cachedLogo = cache[logoUrl]
            when {
                cachedLogo != null -> {
                    Logger.v(TAG, "returning cached logo")
                    callback.onLogoReceived(cachedLogo)
                }
                !connectionsMap.containsKey(logoUrl) -> {
                    val logoConnectionTask = LogoConnectionTask(this, logoUrl, callback)
                    connectionsMap[logoUrl] = logoConnectionTask
                    ThreadManager.EXECUTOR.submit(logoConnectionTask)
                }
                else -> {
                    val existingLogoConnectionTask = connectionsMap[logoUrl]
                    existingLogoConnectionTask?.addCallback(callback)
                }
            }
        }
    }

    /**
     * Cancels a specific request based on the previously sent parameters.
     * If a previous callback exists, it will be triggered as receive failed.
     *
     * @param txVariant The identifier of the transaction variant.
     * @param txSubVariant The identifier of the transaction sub variant.
     * @param size The size if the desired logo;
     */
    fun cancelLogoRequest(txVariant: String, txSubVariant: String?, size: Size?) {
        Logger.d(TAG, "cancelLogoRequest")
        val logoUrl = buildUrl(txVariant, txSubVariant, size)
        synchronized(this) {
            val taskToCancel = connectionsMap.remove(logoUrl)
            if (taskToCancel != null) {
                taskToCancel.cancel(true)
                Logger.d(TAG, "canceled")
            }
        }
    }

    /**
     * Cancels all current requests.
     */
    fun cancelAll() {
        synchronized(this) {
            connectionsMap.values.forEach { it.cancel(true) }
            connectionsMap.clear()
        }
    }

    fun taskFinished(logoUrl: String, logo: BitmapDrawable?) {
        synchronized(this) {
            connectionsMap.remove(logoUrl)
            if (logo != null) cache.put(logoUrl, logo)
        }
    }

    private fun getDensityExtension(densityDpi: Int): String {
        return when {
            densityDpi <= DisplayMetrics.DENSITY_LOW -> "-ldpi"
            densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "" // no extension
            densityDpi <= DisplayMetrics.DENSITY_HIGH -> "-hdpi"
            densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "-xhdpi"
            densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "-xxhdpi"
            else -> "-xxxhdpi"
        }
    }

    private fun buildUrl(txVariant: String, txSubVariant: String?, size: Size?): String {
        val txString =
            if (txSubVariant.isNullOrEmpty()) txVariant
            else "$txVariant/$txSubVariant"
        return String.format(logoUrlFormat, getSizeVariant(size), txString + densityExtension)
    }

    private fun getSizeVariant(size: Size?): String {
        return (size ?: DEFAULT_SIZE).toString()
    }

    /**
     * This method can be called if there is a need to release memory usage.
     */
    private fun clearCache() {
        cache.evictAll()
    }

    private fun isDifferentHost(hostUrl: String): Boolean {
        return !logoUrlFormat.startsWith(hostUrl)
    }

    /**
     * The logo size.
     */
    enum class Size {
        /**
         * Size for small logos (height: 26dp).
         */
        SMALL,

        /**
         * Size for medium logos (height: 50dp).
         */
        MEDIUM,

        /**
         * Size for large logos (height: 100dp).
         */
        LARGE;

        override fun toString(): String {
            return name.lowercase()
        }
    }
}
