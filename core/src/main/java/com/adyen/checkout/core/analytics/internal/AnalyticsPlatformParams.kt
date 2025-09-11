/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import com.adyen.checkout.core.common.internal.helper.CheckoutPlatform
import com.adyen.checkout.core.common.internal.helper.CheckoutPlatformParams

internal object AnalyticsPlatformParams {

    @Suppress("ConstPropertyName", "ktlint:standard:property-naming")
    const val channel = "android"

    val platform: String
        get() = CheckoutPlatformParams.platform.toAnalyticsPlatform().value

    val version: String
        get() = CheckoutPlatformParams.version

    private fun CheckoutPlatform.toAnalyticsPlatform(): AnalyticsPlatform = when (this) {
        CheckoutPlatform.ANDROID -> AnalyticsPlatform.ANDROID
        CheckoutPlatform.FLUTTER -> AnalyticsPlatform.FLUTTER
        CheckoutPlatform.REACT_NATIVE -> AnalyticsPlatform.REACT_NATIVE
    }
}
