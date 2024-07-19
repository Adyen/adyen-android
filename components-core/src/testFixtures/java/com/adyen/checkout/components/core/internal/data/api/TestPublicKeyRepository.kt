/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.core.Environment
import java.io.IOException

class TestPublicKeyRepository : PublicKeyRepository {

    var shouldReturnError = false
    var errorResult = Result.failure<String>(IOException("No internet"))

    override suspend fun fetchPublicKey(
        environment: Environment,
        clientKey: String
    ): Result<String> {
        if (shouldReturnError) return errorResult
        return Result.success(TEST_PUBLIC_KEY)
    }

    companion object {
        const val TEST_PUBLIC_KEY =
            "10001|1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "1111111111111111111111111111111111"
    }
}
