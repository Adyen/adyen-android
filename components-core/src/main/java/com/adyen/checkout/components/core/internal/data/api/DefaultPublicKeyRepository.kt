/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/2/2021.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultPublicKeyRepository(
    private val publicKeyService: PublicKeyService,
) : PublicKeyRepository {

    override suspend fun fetchPublicKey(
        environment: Environment,
        clientKey: String
    ): Result<String> = runSuspendCatching {
        Logger.d(TAG, "fetching publicKey from API")

        retryOnFailure(CONNECTION_RETRIES) {
            publicKeyService.getPublicKey(clientKey).publicKey
        }
    }

    @Suppress("SameParameterValue")
    private inline fun <T> retryOnFailure(times: Int, block: () -> T): T {
        lateinit var throwable: Throwable

        repeat(times) {
            @Suppress("TooGenericExceptionCaught")
            try {
                return block()
            } catch (e: Throwable) {
                throwable = e
            }
        }

        throw throwable
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val CONNECTION_RETRIES = 3
    }
}
