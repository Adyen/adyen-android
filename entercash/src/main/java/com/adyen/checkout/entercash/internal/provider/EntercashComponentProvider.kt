/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.entercash.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.EntercashPaymentMethod
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashComponentState
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.entercash.getEntercashConfiguration
import com.adyen.checkout.entercash.toCheckoutConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

class EntercashComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : IssuerListComponentProvider<
    EntercashComponent,
    EntercashConfiguration,
    EntercashPaymentMethod,
    EntercashComponentState,
    >(
    componentClass = EntercashComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<EntercashPaymentMethod, EntercashComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<EntercashComponentState>
    ) = EntercashComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<EntercashPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = EntercashComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = EntercashPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = EntercashComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): EntercashConfiguration? {
        return checkoutConfiguration.getEntercashConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: EntercashConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
