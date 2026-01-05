/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/12/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.AdyenPaymentFlowKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import kotlinx.coroutines.CoroutineScope

// TODO - error propagation
internal class PaymentFlowStrategyProvider {

    @Suppress("LongParameterList")
    fun get(
        key: AdyenPaymentFlowKey,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
        savedStateHandle: SavedStateHandle,
        componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    ): PaymentFlowStrategy {
        return when (key) {
            is AdyenPaymentFlowKey.PaymentMethod -> {
                getDefaultStrategy(
                    paymentMethod = paymentMethodsApiResponse.getPaymentMethod(key),
                    coroutineScope = coroutineScope,
                    analyticsManager = analyticsManager,
                    checkoutConfiguration = checkoutConfiguration,
                    componentParamsBundle = componentParamsBundle,
                    checkoutCallbacks = checkoutCallbacks,
                    savedStateHandle = savedStateHandle,
                    componentEventHandler = componentEventHandler,
                )
            }

            is AdyenPaymentFlowKey.StoredPaymentMethod -> {
                getDefaultStrategy(
                    paymentMethod = paymentMethodsApiResponse.getStoredPaymentMethod(key),
                    coroutineScope = coroutineScope,
                    analyticsManager = analyticsManager,
                    checkoutConfiguration = checkoutConfiguration,
                    componentParamsBundle = componentParamsBundle,
                    checkoutCallbacks = checkoutCallbacks,
                    savedStateHandle = savedStateHandle,
                    componentEventHandler = componentEventHandler,
                )
            }

            is AdyenPaymentFlowKey.Action -> {
                getActionStrategy(
                    key.action,
                    coroutineScope = coroutineScope,
                    analyticsManager = analyticsManager,
                    checkoutConfiguration = checkoutConfiguration,
                    componentParamsBundle = componentParamsBundle,
                    savedStateHandle = savedStateHandle,
                    componentEventHandler = componentEventHandler,
                )
            }

            else -> error("Unknown key: $key")
        }
    }

    private fun PaymentMethodsApiResponse.getPaymentMethod(
        key: AdyenPaymentFlowKey.PaymentMethod,
    ): PaymentMethodResponse {
        return paymentMethods
            .orEmpty()
            .find { it.type == key.txVariant } ?: error("Cannot find payment method with type: ${key.txVariant}")
    }

    private fun PaymentMethodsApiResponse.getStoredPaymentMethod(
        key: AdyenPaymentFlowKey.StoredPaymentMethod,
    ): PaymentMethodResponse {
        return storedPaymentMethods
            .orEmpty()
            .find { it.id == key.id } ?: error("Cannot find stored payment method with id: ${key.id}")
    }

    @Suppress("LongParameterList")
    private fun getDefaultStrategy(
        paymentMethod: PaymentMethodResponse,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
        savedStateHandle: SavedStateHandle,
        componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    ): DefaultPaymentFlowStrategy {
        val paymentComponent = PaymentMethodProvider.get(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
        )

        val actionProvider = ActionProvider(
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )

        return DefaultPaymentFlowStrategy(
            paymentComponent,
            componentEventHandler,
            actionProvider,
        )
    }

    @Suppress("LongParameterList")
    private fun getActionStrategy(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        savedStateHandle: SavedStateHandle,
        componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    ): ActionPaymentFlowStrategy {
        val actionComponent = ActionComponentProvider.get(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )

        return ActionPaymentFlowStrategy(
            actionComponent = actionComponent,
            componentEventHandler = componentEventHandler,
        )
    }
}
