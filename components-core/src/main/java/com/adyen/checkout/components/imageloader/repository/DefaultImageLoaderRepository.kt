/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 5/12/2022.
 */

package com.adyen.checkout.components.imageloader.repository

import android.graphics.Bitmap
import com.adyen.checkout.components.imageloader.cache.ImageCache
import com.adyen.checkout.components.imageloader.cache.MemoryImageCache
import com.adyen.checkout.components.imageloader.service.ImageLoaderService
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

class DefaultImageLoaderRepository(
    private val cache: ImageCache,
    private val service: ImageLoaderService,
    private val environment: Environment
) : ImageLoaderRepository {

    override suspend fun load(
        imagePath: String
    ): Bitmap? = withContext(Dispatchers.IO) {
        val bitmap = cache.get(imagePath)
        bitmap?.let {
            return@withContext it
        } ?: run {
            service.getImage(imagePath, environment)
                .fold(
                    onSuccess = {
                        cache.put(imagePath, it)
                        it
                    },
                    onFailure = {
                        Logger.e(TAG, "couldn't load image with url ${environment.baseUrl + imagePath} $it")
                        null
                    }
                )
        }
    }

    override fun clearCache() {
        cache.clear()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private var instance: AtomicReference<DefaultImageLoaderRepository>? = null

        @JvmStatic
        fun getInstance(environment: Environment): DefaultImageLoaderRepository {
            val checkInstance = instance
            checkInstance?.get()?.clearCache()
            if (checkInstance == null) {
                val cache = MemoryImageCache()
                val service = ImageLoaderService()
                val created = AtomicReference(
                    DefaultImageLoaderRepository(cache = cache, service = service, environment = environment)
                )
                instance = created
                return created.get()
            }
            return checkInstance.get()
        }
    }
}

