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
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.internal.helper.getLocale
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import kotlinx.coroutines.CoroutineScope

internal class AdvancedPaymentFacilitatorFactory(
    private val applicationContext: Context,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
    private val publicKey: String?,
) : PaymentFacilitatorFactory {

    override fun create(paymentMethod: PaymentMethod, coroutineScope: CoroutineScope): PaymentFacilitator {
        return createPaymentFacilitator(coroutineScope) { analyticsManager, componentParamsBundle ->
            PaymentMethodProvider.get(
                paymentMethod = paymentMethod,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutConfiguration = checkoutConfiguration,
                componentParamsBundle = componentParamsBundle,
                checkoutCallbacks = checkoutCallbacks,
            )
        }
    }

    override fun create(storedPaymentMethod: StoredPaymentMethod, coroutineScope: CoroutineScope): PaymentFacilitator {
        return createPaymentFacilitator(coroutineScope) { analyticsManager, componentParamsBundle ->
            PaymentMethodProvider.get(
                storedPaymentMethod = storedPaymentMethod,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutConfiguration = checkoutConfiguration,
                componentParamsBundle = componentParamsBundle,
                checkoutCallbacks = checkoutCallbacks,
            )
        }
    }

    private fun createPaymentFacilitator(
        coroutineScope: CoroutineScope,
        componentProvider: (AnalyticsManager, ComponentParamsBundle) -> PaymentComponent<BasePaymentComponentState>,
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
        )

        val paymentComponent = componentProvider(analyticsManager, componentParamsBundle)

        val componentEventHandler = AdvancedComponentEventHandler<BasePaymentComponentState>(
            componentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks(),
        )

        val actionProvider = ActionProvider(
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )

        return PaymentFacilitator(
            paymentComponent = paymentComponent,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
            actionProvider = actionProvider,
            checkoutController = checkoutController,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )
    }
}
