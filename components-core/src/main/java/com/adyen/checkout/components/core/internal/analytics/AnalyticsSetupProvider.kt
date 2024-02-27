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
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams

internal interface AnalyticsSetupProvider {
    fun provide(): AnalyticsSetupRequest
}

internal class DefaultAnalyticsSetupProvider(
    private val application: Application,
    private val componentParams: ComponentParams,
    private val source: AnalyticsSource,
    private val sessionId: String?,
) : AnalyticsSetupProvider {

    override fun provide(): AnalyticsSetupRequest {
        return AnalyticsSetupRequest(
            version = AnalyticsPlatformParams.version,
            channel = AnalyticsPlatformParams.channel,
            platform = AnalyticsPlatformParams.platform,
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
    }
}
