/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 5/12/2022.
 */

package com.adyen.checkout.components.imageloader.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageLoaderService {

    suspend fun getImage(imagePath: String, environment: Environment): Result<Bitmap> = withContext(Dispatchers.IO) {
        runSuspendCatching {
            Logger.v(TAG, "call - " + imagePath.hashCode())

            val httpClient = HttpClientFactory.getHttpClient(environment)
            val bytes = httpClient.get(imagePath)

            return@runSuspendCatching BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }


    companion object {
        private val TAG = LogUtil.getTag()
    }
}
