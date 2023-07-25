/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/11/2022.
 */

package com.adyen.checkout.components.core.internal.data.api

import android.os.Build
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BuildConfig
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsMapper {
    internal fun getAnalyticsSetupRequest(
        packageName: String,
        locale: Locale,
        source: AnalyticsSource,
        amount: Amount,
    ): AnalyticsSetupRequest {
        return AnalyticsSetupRequest(
            version = BuildConfig.CHECKOUT_VERSION,
            channel = ANDROID_CHANNEL,
            platform = ANDROID_PLATFORM,
            locale = locale.toString(),
            component = getComponentQueryParameter(source),
            flavor = getFlavorQueryParameter(source),
            deviceBrand = Build.BRAND,
            deviceModel = Build.MODEL,
            referrer = packageName,
            systemVersion = Build.VERSION.SDK_INT.toString(),
            screenWidth = null, // TODO implement
            paymentMethods = null, // TODO implement
            amount = amount,
            level = null, // TODO implement
            containerWidth = null, // unused for Android
        )
    }

    @VisibleForTesting
    internal fun getFlavorQueryParameter(source: AnalyticsSource): String {
        return when (source) {
            is AnalyticsSource.DropIn -> Flavor.DROP_IN
            is AnalyticsSource.PaymentComponent -> {
                if (source.isCreatedByDropIn) Flavor.DROP_IN else Flavor.COMPONENTS
            }
        }.value
    }

    @VisibleForTesting
    internal fun getComponentQueryParameter(source: AnalyticsSource): String {
        return when (source) {
            is AnalyticsSource.DropIn -> DROP_IN_COMPONENT
            is AnalyticsSource.PaymentComponent -> source.paymentMethodType
        }
    }

    private enum class Flavor(val value: String) {
        DROP_IN("dropin"),
        COMPONENTS("components")
    }

    companion object {
        private const val DROP_IN_COMPONENT = "dropin"
        private const val ANDROID_PLATFORM = "android"
        private const val ANDROID_CHANNEL = "Android"
    }
}
