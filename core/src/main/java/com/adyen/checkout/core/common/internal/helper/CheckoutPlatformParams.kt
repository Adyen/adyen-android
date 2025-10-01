/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.BuildConfig
import org.jetbrains.annotations.VisibleForTesting

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CheckoutPlatformParams {

    var platform = CheckoutPlatform.ANDROID
        private set

    var version = BuildConfig.CHECKOUT_VERSION
        private set

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun overrideForCrossPlatform(
        platform: CheckoutPlatform,
        version: String,
    ) {
        CheckoutPlatformParams.platform = platform
        CheckoutPlatformParams.version = version
    }

    @VisibleForTesting
    fun resetDefaults() {
        platform = CheckoutPlatform.ANDROID
        version = BuildConfig.CHECKOUT_VERSION
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CheckoutPlatform {
    ANDROID,
    FLUTTER,
    REACT_NATIVE,
}
