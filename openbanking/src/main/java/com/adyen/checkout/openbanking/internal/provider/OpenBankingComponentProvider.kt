/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.openbanking.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.OpenBankingPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentState
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.openbanking.getOpenBankingConfiguration
import com.adyen.checkout.openbanking.toCheckoutConfiguration

class OpenBankingComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: OldAnalyticsRepository? = null,
) : IssuerListComponentProvider<
    OpenBankingComponent,
    OpenBankingConfiguration,
    OpenBankingPaymentMethod,
    OpenBankingComponentState,
    >(
    componentClass = OpenBankingComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<OpenBankingPaymentMethod, OpenBankingComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OpenBankingComponentState>
    ) = OpenBankingComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<OpenBankingPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OpenBankingComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = OpenBankingPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = OpenBankingComponent.PAYMENT_METHOD_TYPES

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): OpenBankingConfiguration? {
        return checkoutConfiguration.getOpenBankingConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: OpenBankingConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
