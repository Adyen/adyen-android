/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.internal.helper.getLocale
import com.adyen.checkout.core.components.AdyenPaymentFlowKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import kotlinx.coroutines.CoroutineScope

@Suppress("LongParameterList")
internal class AdvancedPaymentFacilitatorFactory(
    private val applicationContext: Context,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
    private val publicKey: String?,
    private val checkoutAttemptId: String?,
    private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : PaymentFacilitatorFactory {

    override fun create(
        key: AdyenPaymentFlowKey,
        coroutineScope: CoroutineScope,
    ): PaymentFacilitator {
        val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = applicationContext.getLocale(),
            dropInOverrideParams = null,
            componentSessionParams = null,
            publicKey = publicKey,
        )

        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParamsBundle.commonComponentParams,
            applicationContext = applicationContext,
            // TODO - Analytics. Provide payment method type to source
            source = AnalyticsSource.PaymentComponent("AwaitAction"),
            sessionId = null,
            checkoutAttemptId = checkoutAttemptId,
        )

        val componentEventHandler = AdvancedComponentEventHandler<BasePaymentComponentState>(
            componentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks(),
        )

        val paymentFlowStrategy = PaymentFlowStrategyProvider().get(
            key = key,
            paymentMethodsApiResponse = paymentMethodsApiResponse,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
            savedStateHandle = savedStateHandle,
            componentEventHandler = componentEventHandler,
        )

        return PaymentFacilitator(
            paymentFlowStrategy = paymentFlowStrategy,
            coroutineScope = coroutineScope,
            checkoutController = checkoutController,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )
    }
}
