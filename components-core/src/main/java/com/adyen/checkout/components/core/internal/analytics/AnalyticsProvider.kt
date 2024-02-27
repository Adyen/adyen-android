/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/11/2022.
 */

package com.adyen.checkout.components.core.internal.analytics

import android.app.Application
import android.os.Build
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.core.BuildConfig
import com.adyen.checkout.components.core.internal.data.api.AnalyticsPlatform
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams

internal interface AnalyticsProvider {
    fun provide(): AnalyticsSetupRequest
}

internal class DefaultAnalyticsProvider(
    val application: Application,
    val componentParams: ComponentParams,
    val source: AnalyticsSource,
    val sessionId: String?,
) : AnalyticsProvider {
    override fun provide(): AnalyticsSetupRequest {
        return AnalyticsSetupRequest(
            version = actualVersion,
            channel = ANDROID_CHANNEL,
            platform = actualPlatform,
            locale = componentParams.shopperLocale.toString(),
            component = getComponentQueryParameter(source),
            flavor = getFlavorQueryParameter(componentParams.isCreatedByDropIn),
            deviceBrand = Build.BRAND,
            deviceModel = Build.MODEL,
            referrer = application.packageName,
            systemVersion = Build.VERSION.SDK_INT.toString(),
            screenWidth = application.resources.displayMetrics.widthPixels,
            paymentMethods = source.getPaymentMethods(),
            amount = componentParams.amount,
            // unused for Android
            containerWidth = null,
            sessionId = sessionId,
        )
    }

    private fun getFlavorQueryParameter(isCreatedByDropIn: Boolean) = if (isCreatedByDropIn) {
        DROP_IN
    } else {
        COMPONENTS
    }

    private fun getComponentQueryParameter(source: AnalyticsSource) = when (source) {
        is AnalyticsSource.DropIn -> DROP_IN
        is AnalyticsSource.PaymentComponent -> source.paymentMethodType
    }

    companion object {
        private const val DROP_IN = "dropin"
        private const val COMPONENTS = "components"
        private const val ANDROID_CHANNEL = "android"

        // these params are prefixed with actual because cross platform SDKs will override them so they are not
        // technically constants
        private var actualPlatform = AnalyticsPlatform.ANDROID.value
        private var actualVersion = BuildConfig.CHECKOUT_VERSION

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun overrideForCrossPlatform(
            platform: AnalyticsPlatform,
            version: String,
        ) {
            this.actualPlatform = platform.value
            this.actualVersion = version
        }

        @VisibleForTesting
        internal fun resetToDefaults() {
            actualPlatform = AnalyticsPlatform.ANDROID.value
            actualVersion = BuildConfig.CHECKOUT_VERSION
        }
    }
}
