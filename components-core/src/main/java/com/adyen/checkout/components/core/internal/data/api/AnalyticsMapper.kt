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
import com.adyen.checkout.components.core.BuildConfig
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsMapper {
    internal fun getQueryParameters(packageName: String, locale: Locale, source: AnalyticsSource): Map<String, String> {
        return mapOf(
            PAYLOAD_VERSION_KEY to CURRENT_PAYLOAD_VERSION_VALUE,
            VERSION_KEY to BuildConfig.CHECKOUT_VERSION,
            FLAVOR_KEY to getFlavorQueryParameter(source),
            COMPONENT_KEY to getComponentQueryParameter(source),
            LOCALE_KEY to locale.toString(),
            PLATFORM_KEY to ANDROID_PLATFORM_VALUE,
            REFERER_KEY to packageName,
            DEVICE_BRAND_KEY to Build.BRAND,
            DEVICE_MODEL_KEY to Build.MODEL,
            SYSTEM_VERSION_KEY to Build.VERSION.SDK_INT.toString(),
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
            is AnalyticsSource.DropIn -> DROP_IN_COMPONENT_VALUE
            is AnalyticsSource.PaymentComponent -> source.paymentMethodType
        }
    }

    private enum class Flavor(val value: String) {
        DROP_IN("dropin"),
        COMPONENTS("components")
    }

    companion object {
        private const val PAYLOAD_VERSION_KEY = "payload_version"
        private const val VERSION_KEY = "version"
        private const val FLAVOR_KEY = "flavor"
        private const val COMPONENT_KEY = "component"
        private const val LOCALE_KEY = "locale"
        private const val PLATFORM_KEY = "platform"
        private const val REFERER_KEY = "referer"
        private const val DEVICE_BRAND_KEY = "device_brand"
        private const val DEVICE_MODEL_KEY = "device_model"
        private const val SYSTEM_VERSION_KEY = "system_version"

        private const val DROP_IN_COMPONENT_VALUE = "dropin"
        private const val CURRENT_PAYLOAD_VERSION_VALUE = "1"
        private const val ANDROID_PLATFORM_VALUE = "android"
    }
}
