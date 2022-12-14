/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.core.image

import com.adyen.checkout.core.api.HttpException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

interface ImageLoader {

    fun load(
        url: String,
        onSuccess: (ByteArray) -> Unit,
        onError: (Throwable) -> Unit
    )
}

object DefaultImageLoader : ImageLoader {

    private val okHttpClient = OkHttpClient()

    @OptIn(DelicateCoroutinesApi::class)
    override fun load(url: String, onSuccess: (ByteArray) -> Unit, onError: (Throwable) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            executeRequest(request, onSuccess, onError)
        }
    }

    private suspend fun executeRequest(request: Request, onSuccess: (ByteArray) -> Unit, onError: (Throwable) -> Unit) {
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val bytes = response.body
                ?.bytes()
                ?: ByteArray(0)

            withContext(Dispatchers.Main) {
                onSuccess(bytes)
            }
        } else {
            onError(HttpException(response.code, response.message, null))
        }
    }
}
