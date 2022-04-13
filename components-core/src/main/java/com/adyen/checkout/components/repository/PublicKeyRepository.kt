/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/2/2021.
 */

package com.adyen.checkout.components.repository

import com.adyen.checkout.components.api.PublicKeyService
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching

class PublicKeyRepository {

    suspend fun fetchPublicKey(
        environment: Environment,
        clientKey: String
    ): Result<String> = runSuspendCatching {
        Logger.d(TAG, "fetching publicKey from API")

        retryOnFailure(CONNECTION_RETRIES) {
            PublicKeyService(environment).getPublicKey(clientKey).publicKey
        }
    }

    @Suppress("SameParameterValue")
    private inline fun <T> retryOnFailure(times: Int, block: () -> T): T {
        repeat(times - 1) {
            @Suppress("TooGenericExceptionCaught")
            try {
                return block()
            } catch (e: Throwable) {
                // Empty body to ignore errors and retry.
            }
        }

        // Final try, that will throw an exception if it fails.
        return block()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val CONNECTION_RETRIES = 3
    }
}
