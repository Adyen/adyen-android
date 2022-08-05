/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/2/2021.
 */

package com.adyen.checkout.components.repository

import com.adyen.checkout.core.api.Environment

interface PublicKeyRepository {

    suspend fun fetchPublicKey(
        environment: Environment,
        clientKey: String
    ): Result<String>
}
