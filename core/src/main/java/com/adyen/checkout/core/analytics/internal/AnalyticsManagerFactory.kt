/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.data.DefaultAnalyticsRepository
import com.adyen.checkout.core.analytics.internal.data.local.ErrorAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.InfoAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.LogAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsRemoteDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsSetupProvider
import com.adyen.checkout.core.analytics.internal.data.remote.api.AnalyticsService
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.internal.ApplicationContextHolder
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsManagerFactory {

    /**
     * Creates an [AnalyticsManager], which makes its setup call right away.
     */
    fun provide(
        params: CheckoutParams,
        source: AnalyticsSource,
        coroutineScope: CoroutineScope,
    ): AnalyticsManager {
        val applicationContext = ApplicationContextHolder.require()

        return DefaultAnalyticsManager(
            analyticsRepository = DefaultAnalyticsRepository(
                localInfoDataStore = InfoAnalyticsLocalDataStore(),
                localLogDataStore = LogAnalyticsLocalDataStore(),
                localErrorDataStore = ErrorAnalyticsLocalDataStore(),
                remoteDataStore = DefaultAnalyticsRemoteDataStore(
                    analyticsService = AnalyticsService(
                        HttpClientFactory.getAnalyticsHttpClient(params.environment),
                    ),
                    clientKey = params.clientKey,
                    infoSize = INFO_SIZE,
                    logSize = LOG_SIZE,
                    errorSize = ERROR_SIZE,
                ),
                analyticsSetupProvider = DefaultAnalyticsSetupProvider(
                    shopperLocale = params.shopperLocale,
                    analyticsLevel = params.analyticsParams.level,
                    packageName = applicationContext.packageName,
                    screenWidth = applicationContext.resources.displayMetrics.widthPixels,
                    amount = params.amount,
                    source = source,
                    sessionId = params.additionalSessionParams?.sessionId,
                    checkoutAttemptId = params.checkoutAttemptId,
                ),
                analyticsTrackRequestProvider = AnalyticsTrackRequestProvider(),
            ),
            analyticsParams = params.analyticsParams,
            checkoutAttemptId = params.checkoutAttemptId,
            coroutineScope = coroutineScope,
        )
    }

    private companion object {
        private const val INFO_SIZE = 50
        private const val LOG_SIZE = 5
        private const val ERROR_SIZE = 5
    }
}
