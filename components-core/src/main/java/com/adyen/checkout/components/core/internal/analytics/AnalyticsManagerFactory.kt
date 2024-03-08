/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.analytics.data.DefaultNewAnalyticsRepository
import com.adyen.checkout.components.core.internal.analytics.data.local.InfoAnalyticsLocalDataStore
import com.adyen.checkout.components.core.internal.analytics.data.local.LogAnalyticsLocalDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.components.core.internal.analytics.data.remote.DefaultAnalyticsRemoteDataStore
import com.adyen.checkout.components.core.internal.analytics.data.remote.DefaultAnalyticsSetupProvider
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsManagerFactory {

    fun provide(
        componentParams: ComponentParams,
        application: Application,
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
        application: Application,
        source: AnalyticsSource,
        sessionId: String?
    ): AnalyticsManager = DefaultAnalyticsManager(
        analyticsRepository = DefaultNewAnalyticsRepository(
            localInfoDataStore = InfoAnalyticsLocalDataStore(),
            localLogDataStore = LogAnalyticsLocalDataStore(),
            remoteDataStore = DefaultAnalyticsRemoteDataStore(
                analyticsService = AnalyticsService(
                    HttpClientFactory.getAnalyticsHttpClient(environment),
                ),
                clientKey = clientKey,
                infoSize = INFO_SIZE,
                logSize = LOG_SIZE,
            ),
            analyticsSetupProvider = DefaultAnalyticsSetupProvider(
                application = application,
                shopperLocale = shopperLocale,
                isCreatedByDropIn = isCreatedByDropIn,
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
    }
}
