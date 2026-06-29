/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.authentication.internal.ui

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.authentication.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.authentication.internal.data.api.SubmitFingerprintService
import com.adyen.checkout.authentication.internal.data.model.AuthenticationSerializer
import com.adyen.checkout.authentication.internal.ui.model.AuthenticationComponentParamsMapper
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope

internal class AuthenticationFactory(private val application: Application) : ActionFactory<AuthenticationComponent> {

    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        savedStateHandle: SavedStateHandle,
    ): AuthenticationComponent {
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val httpClient = HttpClientFactory.getHttpClient(params.environment)
        val submitFingerprintService = SubmitFingerprintService(httpClient)
        val submitFingerprintRepository = SubmitFingerprintRepository(submitFingerprintService)
        val authenticationSerializer = AuthenticationSerializer()

        val threeDS2ComponentParams = AuthenticationComponentParamsMapper().mapToParams(params)

        return AuthenticationComponent(
            action = action,
            componentParams = threeDS2ComponentParams,
            savedStateHandle = savedStateHandle,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            threeDS2Service = ThreeDS2Service.INSTANCE,
            coroutineDispatcher = DispatcherProvider.Default,
            authenticationSerializer = authenticationSerializer,
            application = application,
            clientKey = params.clientKey,
        ).apply {
            initialize(coroutineScope)
        }
    }
}
