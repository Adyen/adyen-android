/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.old

import android.os.Build
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BuildConfig
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import java.util.Locale

// TODO: Remove this file
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsMapper {
//    @Suppress("LongParameterList")
//    internal fun getAnalyticsSetupRequest(
//        packageName: String,
//        locale: Locale,
//        source: AnalyticsSource,
//        amount: Amount?,
//        screenWidth: Long,
//        paymentMethods: List<String>,
//        sessionId: String?,
//    ): AnalyticsSetupRequest {
//        return AnalyticsSetupRequest(
//            version = actualVersion,
//            channel = ANDROID_CHANNEL,
//            platform = actualPlatform,
//            locale = locale.toString(),
//            component = getComponentQueryParameter(source),
//            flavor = getFlavorQueryParameter(source),
//            deviceBrand = Build.BRAND,
//            deviceModel = Build.MODEL,
//            referrer = packageName,
//            systemVersion = Build.VERSION.SDK_INT.toString(),
//            screenWidth = screenWidth,
//            paymentMethods = paymentMethods,
//            amount = amount,
//            // unused for Android
//            containerWidth = null,
//            sessionId = sessionId,
//        )
//    }
//
//    @VisibleForTesting
//    internal fun getFlavorQueryParameter(source: AnalyticsSource): String {
//        return when (source) {
//            is AnalyticsSource.DropIn -> Flavor.DROP_IN
//            is AnalyticsSource.PaymentComponent -> {
//                if (source.isCreatedByDropIn) Flavor.DROP_IN else Flavor.COMPONENTS
//            }
//        }.value
//    }
//
//    @VisibleForTesting
//    internal fun getComponentQueryParameter(source: AnalyticsSource): String {
//        return when (source) {
//            is AnalyticsSource.DropIn -> DROP_IN_COMPONENT
//            is AnalyticsSource.PaymentComponent -> source.paymentMethodType
//        }
//    }
//
//    private enum class Flavor(val value: String) {
//        DROP_IN("dropin"),
//        COMPONENTS("components")
//    }
//
//    companion object {
//        private const val DROP_IN_COMPONENT = "dropin"
//        private const val ANDROID_CHANNEL = "android"
//
//        // these params are prefixed with actual because cross platform SDKs will override them so they are not
//        // technically constants
//        private var actualPlatform = AnalyticsPlatform.ANDROID.value
//        private var actualVersion = BuildConfig.CHECKOUT_VERSION
//
//        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
//        fun overrideForCrossPlatform(
//            platform: AnalyticsPlatform,
//            version: String,
//        ) {
//            this.actualPlatform = platform.value
//            this.actualVersion = version
//        }
//
//        @VisibleForTesting
//        internal fun resetToDefaults() {
//            actualPlatform = AnalyticsPlatform.ANDROID.value
//            actualVersion = BuildConfig.CHECKOUT_VERSION
//        }
//    }
}
