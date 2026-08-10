/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/12/2025.
 */

package com.adyen.checkout.core.error.internal

import androidx.annotation.RestrictTo

/**
 * Sealed base class for all internal SDK errors.
 *
 * All internal error types extend this class directly. Being sealed ensures exhaustive handling
 * in `when` expressions, so adding a new error type forces a compiler error at all handling sites.
 *
 * The SDK reports failures through three distinct mechanisms. Pick the matching one when adding
 * new failure handling:
 *
 * 1. Configuration that can be checked up front returns a `CheckoutError` instead of throwing. See
 * `CheckoutConfiguration.validate()`, which surfaces the error as `Checkout.Result.Error` before a
 * controller exists.
 *
 * 2. Failures a correctly integrated app can hit at runtime are represented by an
 * [InternalCheckoutError]. They are mapped with `toCheckoutError()` and delivered to the merchant,
 * either through `onFailure` while the flow is running, or as a `CheckoutException` when they occur
 * while the controller is being created.
 *
 * 3. Broken invariants that can only happen when something is wired incorrectly use plain runtime
 * exceptions such as `IllegalStateException` and `IllegalArgumentException`. These are deliberately
 * not converted, so that they fail fast and surface as bugs rather than being reported as checkout
 * errors.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class InternalCheckoutError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
