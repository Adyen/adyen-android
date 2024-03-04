/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/1/2023.
 */

package com.adyen.checkout.payeasy.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.PayEasyPaymentMethod
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyComponentState
import com.adyen.checkout.payeasy.PayEasyConfiguration
import com.adyen.checkout.payeasy.getPayEasyConfiguration
import com.adyen.checkout.payeasy.toCheckoutConfiguration

class PayEasyComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: OldAnalyticsRepository? = null,
) : EContextComponentProvider<PayEasyComponent, PayEasyConfiguration, PayEasyPaymentMethod, PayEasyComponentState>(
    componentClass = PayEasyComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponentState(
        data: PaymentComponentData<PayEasyPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = PayEasyComponentState(data, isInputValid, isReady)

    override fun createComponent(
        delegate: EContextDelegate<PayEasyPaymentMethod, PayEasyComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PayEasyComponentState>,
    ): PayEasyComponent {
        return PayEasyComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    override fun createPaymentMethod(): PayEasyPaymentMethod {
        return PayEasyPaymentMethod()
    }

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): PayEasyConfiguration? {
        return checkoutConfiguration.getPayEasyConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: PayEasyConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return PayEasyComponent.PAYMENT_METHOD_TYPES
    }
}
