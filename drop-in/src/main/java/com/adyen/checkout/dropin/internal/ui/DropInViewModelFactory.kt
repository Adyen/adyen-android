/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepositoryData
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.OrderStatusRepository
import com.adyen.checkout.components.core.internal.data.api.OrderStatusService
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.util.screenWidthPixels
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.dropin.DropInConfiguration

internal class DropInViewModelFactory(
    activity: ComponentActivity
) : AbstractSavedStateViewModelFactory(activity, activity.intent.extras) {

    private val packageName: String = activity.packageName
    private val screenWidth: Int = activity.screenWidthPixels

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        val bundleHandler = DropInSavedStateHandleContainer(handle)

        val dropInConfiguration: DropInConfiguration = requireNotNull(bundleHandler.dropInConfiguration)
        val amount: Amount? = bundleHandler.amount
        val paymentMethods = bundleHandler.paymentMethodsApiResponse?.paymentMethods?.mapNotNull { it.type }.orEmpty()
        val session = bundleHandler.sessionDetails

        val httpClient = HttpClientFactory.getHttpClient(dropInConfiguration.environment)
        val orderStatusRepository = OrderStatusRepository(OrderStatusService(httpClient))
        val analyticsRepository = DefaultAnalyticsRepository(
            analyticsRepositoryData = AnalyticsRepositoryData(
                level = AnalyticsParams(dropInConfiguration.analyticsConfiguration).level,
                packageName = packageName,
                locale = dropInConfiguration.shopperLocale,
                source = AnalyticsSource.DropIn(),
                clientKey = dropInConfiguration.clientKey,
                amount = amount,
                screenWidth = screenWidth,
                paymentMethods = paymentMethods,
                sessionId = session?.id,
            ),
            analyticsService = AnalyticsService(
                HttpClientFactory.getAnalyticsHttpClient(dropInConfiguration.environment)
            ),
            analyticsMapper = AnalyticsMapper(),
        )

        @Suppress("UNCHECKED_CAST")
        return DropInViewModel(bundleHandler, orderStatusRepository, analyticsRepository) as T
    }
}
