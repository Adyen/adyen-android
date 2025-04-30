/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.internal.util

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CancellationException

@Suppress("TooGenericExceptionCaught")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <T, R> T.runSuspendCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
