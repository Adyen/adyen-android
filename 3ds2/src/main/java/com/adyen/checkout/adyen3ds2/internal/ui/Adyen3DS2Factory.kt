/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintService
import com.adyen.checkout.adyen3ds2.internal.data.model.Adyen3DS2Serializer
import com.adyen.checkout.adyen3ds2.internal.ui.model.Adyen3DS2ComponentParamsMapper
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope

internal class Adyen3DS2Factory(private val application: Application) : ActionFactory<Adyen3DS2Component> {
    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        commonComponentParams: CommonComponentParams,
    ): Adyen3DS2Component {
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val httpClient = HttpClientFactory.getHttpClient(commonComponentParams.environment)
        val submitFingerprintService = SubmitFingerprintService(httpClient)
        val submitFingerprintRepository = SubmitFingerprintRepository(submitFingerprintService)
        val adyen3DS2DetailsSerializer = Adyen3DS2Serializer()

        val adyen3DS2ComponentParams =
            Adyen3DS2ComponentParamsMapper().mapToParams(checkoutConfiguration, commonComponentParams)

        val adyen3DS2Delegate = Adyen3DS2Delegate(
            action = action,
            componentParams = adyen3DS2ComponentParams,
            savedStateHandle = savedStateHandle,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            threeDS2Service = ThreeDS2Service.INSTANCE,
            coroutineDispatcher = DispatcherProvider.Default,
            adyen3DS2Serializer = adyen3DS2DetailsSerializer,
            application = application,
        ).apply {
            initialize(coroutineScope)
        }

        return Adyen3DS2Component(
            adyen3DS2Delegate = adyen3DS2Delegate,
        )
    }
}
