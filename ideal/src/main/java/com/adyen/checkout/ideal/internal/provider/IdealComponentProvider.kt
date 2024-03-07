/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.ideal.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.IdealPaymentMethod
import com.adyen.checkout.ideal.IdealComponent
import com.adyen.checkout.ideal.IdealComponentState
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.ideal.getIdealConfiguration
import com.adyen.checkout.ideal.toCheckoutConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

class IdealComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : IssuerListComponentProvider<IdealComponent, IdealConfiguration, IdealPaymentMethod, IdealComponentState>(
    componentClass = IdealComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<IdealPaymentMethod, IdealComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<IdealComponentState>
    ) = IdealComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<IdealPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = IdealComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = IdealPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = IdealComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): IdealConfiguration? {
        return checkoutConfiguration.getIdealConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: IdealConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
