/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/8/2022.
 */

package com.adyen.checkout.components.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.core.api.Environment
import java.io.IOException

/**
 * Test implementation of [PublicKeyRepository]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
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
