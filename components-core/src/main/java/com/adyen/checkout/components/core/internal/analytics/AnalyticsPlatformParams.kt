/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.BuildConfig

object AnalyticsPlatformParams {

    @Suppress("ConstPropertyName")
    const val channel = "android"

    var platform = AnalyticsPlatform.ANDROID.value
        private set

    var version = BuildConfig.CHECKOUT_VERSION
        private set

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun overrideForCrossPlatform(
        platform: AnalyticsPlatform,
        version: String,
    ) {
        this.platform = platform.value
        this.version = version
    }

    @VisibleForTesting
    internal fun resetToDefaults() {
        platform = AnalyticsPlatform.ANDROID.value
        version = BuildConfig.CHECKOUT_VERSION
    }
}
