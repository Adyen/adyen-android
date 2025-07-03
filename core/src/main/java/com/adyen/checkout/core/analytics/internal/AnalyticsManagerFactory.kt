/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.data.DefaultAnalyticsRepository
import com.adyen.checkout.core.analytics.internal.data.local.ErrorAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.InfoAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.local.LogAnalyticsLocalDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsRemoteDataStore
import com.adyen.checkout.core.analytics.internal.data.remote.DefaultAnalyticsSetupProvider
import com.adyen.checkout.core.analytics.internal.data.remote.api.AnalyticsService
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsManagerFactory {

    fun provide(
        componentParams: ComponentParams,
        application: Application?,
        source: AnalyticsSource,
        sessionId: String?
    ): AnalyticsManager = provide(
        shopperLocale = componentParams.shopperLocale,
        environment = componentParams.environment,
        clientKey = componentParams.clientKey,
        analyticsParams = componentParams.analyticsParams,
        isCreatedByDropIn = componentParams.isCreatedByDropIn,
        amount = componentParams.amount,
        application = application,
        source = source,
        sessionId = sessionId,
    )

    @Suppress("LongParameterList")
    fun provide(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        analyticsParams: AnalyticsParams,
        isCreatedByDropIn: Boolean,
        amount: Amount?,
        application: Application?,
        source: AnalyticsSource,
        sessionId: String?
    ): AnalyticsManager = DefaultAnalyticsManager(
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
                application = application,
                shopperLocale = shopperLocale,
                isCreatedByDropIn = isCreatedByDropIn,
                analyticsLevel = analyticsParams.level,
                amount = amount,
                source = source,
                sessionId = sessionId,
            ),
            analyticsTrackRequestProvider = AnalyticsTrackRequestProvider(),
        ),
        analyticsParams = analyticsParams,
    )

    companion object {
        private const val INFO_SIZE = 50
        private const val LOG_SIZE = 5
        private const val ERROR_SIZE = 5
    }
}
