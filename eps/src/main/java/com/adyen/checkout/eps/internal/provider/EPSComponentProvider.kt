/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.eps.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.EPSPaymentMethod
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSComponentState
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.eps.getEPSConfiguration
import com.adyen.checkout.eps.toCheckoutConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

class EPSComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: OldAnalyticsRepository? = null,
) : IssuerListComponentProvider<EPSComponent, EPSConfiguration, EPSPaymentMethod, EPSComponentState>(
    componentClass = EPSComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
    hideIssuerLogosDefaultValue = true,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<EPSPaymentMethod, EPSComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<EPSComponentState>
    ) = EPSComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<EPSPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = EPSComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = EPSPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = EPSComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): EPSConfiguration? {
        return checkoutConfiguration.getEPSConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: EPSConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
