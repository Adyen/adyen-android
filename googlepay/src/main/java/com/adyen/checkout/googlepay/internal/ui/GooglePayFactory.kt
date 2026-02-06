/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.app.Application
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.googlepay.getGooglePayConfiguration
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParamsMapper
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayPaymentComponentState
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.google.android.gms.wallet.Wallet
import kotlinx.coroutines.CoroutineScope

internal class GooglePayFactory : PaymentComponentFactory<GooglePayPaymentComponentState, GooglePayComponent> {

    @Suppress("UNUSED_PARAMETER")
    override fun create(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks
    ): GooglePayComponent {
        val googlePayComponentParams = GooglePayComponentParamsMapper().mapToParams(
            componentParamsBundle = componentParamsBundle,
            googlePayConfiguration = checkoutConfiguration.getGooglePayConfiguration(),
            paymentMethod = paymentMethod,
        )

        val componentStateFactory = GooglePayComponentStateFactory()
        val componentStateReducer = GooglePayComponentStateReducer()
        val componentStateValidator = GooglePayComponentStateValidator()
        val viewStateProducer = GooglePayViewStateProducer()

        val paymentsClient = Wallet.getPaymentsClient(
            application,
            GooglePayUtils.createWalletOptions(googlePayComponentParams),
        )

        val googlePayAvailabilityCheck = GooglePayAvailabilityCheck(application)

        return GooglePayComponent(
            componentParams = googlePayComponentParams,
            analyticsManager = analyticsManager,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            paymentsClient = paymentsClient,
            coroutineScope = coroutineScope,
            paymentMethod = paymentMethod,
            googlePayAvailabilityCheck = googlePayAvailabilityCheck,
            componentStateValidator = componentStateValidator,
            componentStateFactory = componentStateFactory,
            componentStateReducer = componentStateReducer,
            viewStateProducer = viewStateProducer,
        )
    }
}
