/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.repositories

import android.util.Log
import com.adyen.checkout.example.extensions.IODispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> safeApiCall(call: suspend () -> T): T? = withContext(IODispatcher) {
    return@withContext try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Log.e("CO.safeApiCall", "API call failed", e)
        null
    }
}
