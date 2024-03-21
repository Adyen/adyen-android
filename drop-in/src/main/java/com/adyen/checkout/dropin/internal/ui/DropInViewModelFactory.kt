/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.internal.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.data.api.OrderStatusRepository
import com.adyen.checkout.components.core.internal.data.api.OrderStatusService
import com.adyen.checkout.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.dropin.internal.ui.model.DropInParams
import com.adyen.checkout.dropin.internal.ui.model.DropInParamsMapper
import com.adyen.checkout.dropin.internal.ui.model.DropInPaymentMethodInformation
import com.adyen.checkout.dropin.internal.ui.model.overrideInformation
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import java.util.Locale

internal class DropInViewModelFactory(
    activity: ComponentActivity,
    localeProvider: LocaleProvider = LocaleProvider(),
) : AbstractSavedStateViewModelFactory(activity, activity.intent.extras) {

    private val deviceLocale: Locale = localeProvider.getLocale(activity)
    private val application: Application = activity.application

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        val bundleHandler = DropInSavedStateHandleContainer(handle)

        val dropInParams = getDropInParams(
            checkoutConfiguration = requireNotNull(bundleHandler.checkoutConfiguration),
            sessionDetails = bundleHandler.sessionDetails,
        )

        bundleHandler.overridePaymentMethodInformation(dropInParams.overriddenPaymentMethodInformation)

        val paymentMethods = bundleHandler.paymentMethodsApiResponse?.paymentMethods?.mapNotNull { it.type }.orEmpty()

        val httpClient = HttpClientFactory.getHttpClient(dropInParams.environment)
        val orderStatusRepository = OrderStatusRepository(OrderStatusService(httpClient))
        val analyticsManager = AnalyticsManagerFactory().provide(
            shopperLocale = dropInParams.shopperLocale,
            environment = dropInParams.environment,
            clientKey = dropInParams.clientKey,
            analyticsParams = dropInParams.analyticsParams,
            isCreatedByDropIn = true,
            amount = dropInParams.amount,
            application = application,
            source = AnalyticsSource.DropIn(paymentMethods),
            sessionId = bundleHandler.sessionDetails?.id,
        )

        @Suppress("UNCHECKED_CAST")
        return DropInViewModel(
            bundleHandler,
            orderStatusRepository,
            analyticsManager,
            dropInParams,
        ) as T
    }

    private fun getDropInParams(
        checkoutConfiguration: CheckoutConfiguration,
        sessionDetails: SessionDetails?
    ): DropInParams {
        val sessionParams = sessionDetails?.let { SessionParamsFactory.create(sessionDetails) }
        return DropInParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = deviceLocale,
            sessionParams = sessionParams,
        )
    }
}

internal fun DropInSavedStateHandleContainer.overridePaymentMethodInformation(
    paymentMethodInformationMap: Map<String, DropInPaymentMethodInformation>
) {
    paymentMethodInformationMap.forEach { informationEntry ->
        val type = informationEntry.key
        val paymentMethodInformation = informationEntry.value

        paymentMethodsApiResponse?.paymentMethods
            ?.filter { paymentMethod -> paymentMethod.type == type }
            ?.forEach { paymentMethod -> paymentMethod.overrideInformation(paymentMethodInformation) }
    }
}
