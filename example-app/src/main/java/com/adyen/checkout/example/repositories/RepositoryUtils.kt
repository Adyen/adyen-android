/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> safeApiCall(call: suspend () -> T): T? = withContext(Dispatchers.IO) {
    runSuspendCatching { call() }
        .fold(
            onSuccess = { it },
            onFailure = { e ->
                Logger.e("safeApiCall", "API call failed", e)
                null
            }
        )
}
