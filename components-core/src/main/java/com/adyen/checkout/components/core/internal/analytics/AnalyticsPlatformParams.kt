/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/2/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import com.adyen.checkout.components.core.internal.util.CheckoutPlatform
import com.adyen.checkout.components.core.internal.util.CheckoutPlatformParams

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
