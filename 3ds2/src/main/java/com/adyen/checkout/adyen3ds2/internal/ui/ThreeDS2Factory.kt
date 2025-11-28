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
import com.adyen.checkout.adyen3ds2.internal.data.model.ThreeDS2Serializer
import com.adyen.checkout.adyen3ds2.internal.ui.model.ThreeDS2ComponentParamsMapper
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

internal class ThreeDS2Factory(private val application: Application) : ActionFactory<ThreeDS2Component> {
    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        commonComponentParams: CommonComponentParams,
    ): ThreeDS2Component {
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val httpClient = HttpClientFactory.getHttpClient(commonComponentParams.environment)
        val submitFingerprintService = SubmitFingerprintService(httpClient)
        val submitFingerprintRepository = SubmitFingerprintRepository(submitFingerprintService)
        val threeDS2Serializer = ThreeDS2Serializer()

        val threeDS2ComponentParams =
            ThreeDS2ComponentParamsMapper().mapToParams(checkoutConfiguration, commonComponentParams)

        return ThreeDS2Component(
            action = action,
            componentParams = threeDS2ComponentParams,
            savedStateHandle = savedStateHandle,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            threeDS2Service = ThreeDS2Service.INSTANCE,
            coroutineDispatcher = DispatcherProvider.Default,
            threeDS2Serializer = threeDS2Serializer,
            application = application,
        ).apply {
            initialize(coroutineScope)
        }
    }
}
