/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import com.adyen.checkout.core.analytics.internal.data.DefaultAnalyticsRepository
import com.adyen.checkout.core.analytics.internal.data.local.ErrorAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.InfoAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.LogAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsRemoteDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsSetupProvider
import com.adyen.checkout.core.analytics.internal.data.remote.api.AnalyticsService
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.IntegrationType
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.ApplicationContextHolder
import java.util.Locale

internal class AnalyticsManagerFactory {

    fun provide(
        params: CheckoutParams,
        source: AnalyticsSource,
        sessionId: String?,
        checkoutAttemptId: String?,
    ): AnalyticsManager = provide(
        shopperLocale = params.shopperLocale,
        environment = params.environment,
        clientKey = params.clientKey,
        analyticsParams = params.analyticsParams,
        integrationType = params.integrationType,
        amount = params.amount,
        source = source,
        sessionId = sessionId,
        checkoutAttemptId = checkoutAttemptId,
    )

    @Suppress("LongParameterList")
    fun provide(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        analyticsParams: AnalyticsParams,
        integrationType: IntegrationType,
        amount: Amount?,
        source: AnalyticsSource,
        sessionId: String?,
        checkoutAttemptId: String?,
    ): AnalyticsManager {
        val applicationContext = ApplicationContextHolder.require()

        return DefaultAnalyticsManager(
            analyticsRepository = DefaultAnalyticsRepository(
                localInfoDataStore = InfoAnalyticsLocalDataStore(),
                localLogDataStore = LogAnalyticsLocalDataStore(),
                localErrorDataStore = ErrorAnalyticsLocalDataStore(),
                remoteDataStore = DefaultAnalyticsRemoteDataStore(
                    analyticsService = AnalyticsService(
                        HttpClientFactory.getAnalyticsHttpClient(environment),
                    ),
                    clientKey = clientKey,
                    infoSize = INFO_SIZE,
                    logSize = LOG_SIZE,
                    errorSize = ERROR_SIZE,
                ),
                analyticsSetupProvider = DefaultAnalyticsSetupProvider(
                    shopperLocale = shopperLocale,
                    integrationType = integrationType,
                    analyticsLevel = analyticsParams.level,
                    packageName = applicationContext.packageName,
                    screenWidth = applicationContext.resources.displayMetrics.widthPixels,
                    amount = amount,
                    source = source,
                    sessionId = sessionId,
                    checkoutAttemptId = checkoutAttemptId,
                ),
                analyticsTrackRequestProvider = AnalyticsTrackRequestProvider(),
            ),
            analyticsParams = analyticsParams,
        )
    }

    companion object {
        private const val INFO_SIZE = 50
        private const val LOG_SIZE = 5
        private const val ERROR_SIZE = 5
    }
}
