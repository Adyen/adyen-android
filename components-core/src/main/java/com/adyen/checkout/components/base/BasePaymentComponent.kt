/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components.base

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticEvent.Companion.create
import com.adyen.checkout.components.analytics.AnalyticEvent.Flavor
import com.adyen.checkout.components.analytics.AnalyticsDispatcher.Companion.dispatchEvent
import com.adyen.checkout.components.base.lifecycle.PaymentComponentViewModel
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil

@Suppress("TooManyFunctions")
abstract class BasePaymentComponent<
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>
    >(
    savedStateHandle: SavedStateHandle,
    private val paymentMethodDelegate: PaymentMethodDelegate,
    configuration: ConfigurationT
) : PaymentComponentViewModel<ConfigurationT, ComponentStateT>(savedStateHandle, configuration) {

    private var isCreatedForDropIn = false
    private var isAnalyticsEnabled = true

    /**
     * Sets if the analytics events can be sent by the component.
     * Default is True.
     *
     * @param isEnabled Is analytics should be enabled or not.
     */
    // TODO: 13/11/2020 Add to Configuration instead?
    fun setAnalyticsEnabled(isEnabled: Boolean) {
        isAnalyticsEnabled = isEnabled
    }

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    // TODO change later when analytics are implemented
    fun sendAnalyticsEvent(context: Context) {
        if (isAnalyticsEnabled) {
            val flavor: Flavor = if (isCreatedForDropIn) {
                Flavor.DROPIN
            } else {
                Flavor.COMPONENT
            }
            val type = paymentMethodDelegate.getPaymentMethodType()
            if (type.isEmpty()) {
                throw CheckoutException("Payment method has empty or null type")
            }
            val analyticEvent = create(context, flavor, type, configuration.shopperLocale)
            dispatchEvent(context, configuration.environment, analyticEvent)
        }
    }

    fun setCreatedForDropIn() {
        isCreatedForDropIn = true
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
