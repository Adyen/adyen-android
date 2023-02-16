/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/1/2023.
 */

package com.adyen.checkout.payeasy.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.PayEasyPaymentMethod
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.payeasy.PayEasyComponent
import com.adyen.checkout.payeasy.PayEasyConfiguration
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PayEasyComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : EContextComponentProvider<PayEasyComponent, PayEasyConfiguration, PayEasyPaymentMethod>(
    componentClass = PayEasyComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {
    override fun createComponent(
        delegate: EContextDelegate<PayEasyPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<PayEasyPaymentMethod>>,
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

    override fun getSupportedPaymentMethods(): List<String> {
        return PayEasyComponent.PAYMENT_METHOD_TYPES
    }
}
