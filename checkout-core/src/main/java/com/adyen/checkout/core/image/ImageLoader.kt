/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.core.image

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
        onSuccess: (Bitmap) -> Unit,
        onError: (Throwable) -> Unit
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DefaultImageLoader : ImageLoader {
    private val okHttpClient = OkHttpClient()

    override suspend fun load(url: String, onSuccess: (Bitmap) -> Unit, onError: (Throwable) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        load(request, onSuccess, onError)
    }

    private suspend fun load(
        request: Request,
        onSuccess: (Bitmap) -> Unit,
        onError: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(request)

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                val bytes = response.body
                    ?.bytes()
                    ?: ByteArray(0)

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                withContext(Dispatchers.Main) {
                    onSuccess(bitmap)
                }
            } else {
                onError(HttpException(response.code, response.message, null))
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: IOException) {
            onError(e)
        }
    }
}
