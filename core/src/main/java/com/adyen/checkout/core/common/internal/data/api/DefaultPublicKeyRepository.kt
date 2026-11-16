/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/2/2021.
 */

package com.adyen.checkout.core.common.internal.data.api

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.CheckoutLogLevel

internal class DefaultPublicKeyRepository(
    private val publicKeyService: PublicKeyService,
) : PublicKeyRepository {

    override suspend fun fetchPublicKey(
        environment: Environment,
        clientKey: String
    ): Result<String> = runSuspendCatching {
        adyenLog(CheckoutLogLevel.DEBUG) { "fetching publicKey from API" }

        publicKeyService.getPublicKey(clientKey).publicKey
    }
}
