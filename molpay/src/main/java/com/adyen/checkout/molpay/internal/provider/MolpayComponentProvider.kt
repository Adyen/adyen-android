/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.molpay.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.MolpayPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.molpay.MolpayComponent
import com.adyen.checkout.molpay.MolpayComponentState
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.molpay.getMolpayConfiguration
import com.adyen.checkout.molpay.toCheckoutConfiguration

class MolpayComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: OldAnalyticsRepository? = null,
) : IssuerListComponentProvider<MolpayComponent, MolpayConfiguration, MolpayPaymentMethod, MolpayComponentState>(
    componentClass = MolpayComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<MolpayPaymentMethod, MolpayComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<MolpayComponentState>
    ) = MolpayComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<MolpayPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = MolpayComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = MolpayPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = MolpayComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): MolpayConfiguration? {
        return checkoutConfiguration.getMolpayConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: MolpayConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
