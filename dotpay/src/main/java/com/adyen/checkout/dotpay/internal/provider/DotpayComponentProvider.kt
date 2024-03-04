/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.dotpay.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.DotpayPaymentMethod
import com.adyen.checkout.dotpay.DotpayComponent
import com.adyen.checkout.dotpay.DotpayComponentState
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dotpay.getDotpayConfiguration
import com.adyen.checkout.dotpay.toCheckoutConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

class DotpayComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: OldAnalyticsRepository? = null,
) : IssuerListComponentProvider<DotpayComponent, DotpayConfiguration, DotpayPaymentMethod, DotpayComponentState>(
    componentClass = DotpayComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<DotpayPaymentMethod, DotpayComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<DotpayComponentState>
    ) = DotpayComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<DotpayPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = DotpayComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = DotpayPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = DotpayComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): DotpayConfiguration? {
        return checkoutConfiguration.getDotpayConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: DotpayConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
