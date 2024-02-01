/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingPLPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentState
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingpl.getOnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingpl.toCheckoutConfiguration

class OnlineBankingPLComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    isCreatedByDropIn: Boolean = false,
    overrideSessionParams: SessionParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : IssuerListComponentProvider<
    OnlineBankingPLComponent,
    OnlineBankingPLConfiguration,
    OnlineBankingPLPaymentMethod,
    OnlineBankingPLComponentState
    >(
    componentClass = OnlineBankingPLComponent::class.java,
    isCreatedByDropIn = isCreatedByDropIn,
    overrideSessionParams = overrideSessionParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<OnlineBankingPLPaymentMethod, OnlineBankingPLComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OnlineBankingPLComponentState>
    ) = OnlineBankingPLComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<OnlineBankingPLPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OnlineBankingPLComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = OnlineBankingPLPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = OnlineBankingPLComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): OnlineBankingPLConfiguration? {
        return checkoutConfiguration.getOnlineBankingPLConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: OnlineBankingPLConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
