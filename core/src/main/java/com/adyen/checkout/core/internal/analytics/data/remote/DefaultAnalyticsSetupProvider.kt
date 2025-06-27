/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.analytics.data.remote

import android.app.Application
import android.os.Build
import com.adyen.checkout.core.data.model.Amount
import com.adyen.checkout.core.internal.analytics.AnalyticsPlatformParams
import com.adyen.checkout.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.core.internal.data.model.AnalyticsSetupRequest
import com.adyen.checkout.core.analytics.internal.AnalyticsParamsLevel
import java.util.Locale

@Suppress("LongParameterList")
internal class DefaultAnalyticsSetupProvider(
    // TODO - Analytics, application should not be null, or we should ask for another data object
    private val application: Application?,
    private val shopperLocale: Locale,
    private val isCreatedByDropIn: Boolean,
    private val analyticsLevel: AnalyticsParamsLevel,
    private val amount: Amount?,
    private val source: AnalyticsSource,
    private val sessionId: String?,
) : AnalyticsSetupProvider {

    override fun provide(): AnalyticsSetupRequest {
        return AnalyticsSetupRequest(
            version = AnalyticsPlatformParams.version,
            channel = AnalyticsPlatformParams.channel,
            platform = AnalyticsPlatformParams.platform,
            locale = shopperLocale.toLanguageTag(),
            component = getComponentQueryParameter(source),
            flavor = getFlavorQueryParameter(isCreatedByDropIn),
            level = getLevelQueryParameter(analyticsLevel),
            deviceBrand = Build.BRAND,
            deviceModel = Build.MODEL,
            referrer = application?.packageName ?: "",
            systemVersion = Build.VERSION.SDK_INT.toString(),
            screenWidth = application?.resources?.displayMetrics?.widthPixels ?: 0,
            paymentMethods = source.getPaymentMethods(),
            amount = amount,
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

    private fun getLevelQueryParameter(analyticsParamsLevel: AnalyticsParamsLevel) = when (analyticsParamsLevel) {
        AnalyticsParamsLevel.INITIAL -> ANALYTICS_LEVEL_INITIAL
        AnalyticsParamsLevel.ALL -> ANALYTICS_LEVEL_ALL
    }

    companion object {
        private const val DROP_IN = "dropin"
        private const val COMPONENTS = "components"

        private const val ANALYTICS_LEVEL_INITIAL = "initial"
        private const val ANALYTICS_LEVEL_ALL = "all"
    }
}
