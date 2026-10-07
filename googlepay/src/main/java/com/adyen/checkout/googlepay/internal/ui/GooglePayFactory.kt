/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.internal.ApplicationContextHolder
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.PaymentMethodAvailabilityCheck
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParamsMapper
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import kotlinx.coroutines.CoroutineScope

internal class GooglePayFactory @VisibleForTesting constructor(
    private val availabilityCheckFor: (GooglePayComponentParams) -> GooglePayAvailabilityCheck,
) : PaymentComponentFactory<GooglePayComponent>,
    PaymentMethodAvailabilityCheck {

    constructor() : this(
        availabilityCheckFor = { componentParams ->
            GooglePayAvailabilityCheck(
                componentParams = componentParams,
                applicationContext = ApplicationContextHolder.require(),
            )
        },
    )

    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): GooglePayComponent {
        val componentParams = mapToComponentParams(paymentMethod, params)

        return GooglePayComponent(
            analyticsManager = analyticsManager,
            componentParams = componentParams,
            sdkDataProvider = sdkDataProvider,
            googlePayAvailabilityCheck = availabilityCheckFor(componentParams),
            paymentMethodType = paymentMethod.type,
            componentStateValidator = GooglePayComponentStateValidator(),
            componentStateFactory = GooglePayComponentStateFactory(componentParams),
            componentStateReducer = GooglePayComponentStateReducer(),
            viewStateProducer = GooglePayViewStateProducer(showSubmitButton = params.showSubmitButton),
            coroutineScope = coroutineScope,
        )
    }

    override suspend fun isAvailable(paymentMethod: PaymentMethod, params: CheckoutParams): Boolean {
        return availabilityCheckFor(mapToComponentParams(paymentMethod, params)).isAvailable()
    }

    private fun mapToComponentParams(
        paymentMethod: PaymentMethod,
        params: CheckoutParams,
    ): GooglePayComponentParams {
        // TODO - Remove casting when paymentMethod object is typed
        val googlePayPaymentMethod = paymentMethod as? GooglePayPaymentMethod
            ?: throw IllegalArgumentException("Incorrect paymentMethod")
        return GooglePayComponentParamsMapper().mapToParams(
            params = params,
            paymentMethod = googlePayPaymentMethod,
        )
    }
}
