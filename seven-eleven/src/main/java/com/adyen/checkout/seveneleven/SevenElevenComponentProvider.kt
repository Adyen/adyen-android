/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/1/2023.
 */

package com.adyen.checkout.seveneleven

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.SevenElevenPaymentMethod
import com.adyen.checkout.econtext.EContextComponentProvider
import com.adyen.checkout.econtext.EContextDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SevenElevenComponentProvider(
    overrideComponentParams: ComponentParams? = null,
) : EContextComponentProvider<SevenElevenComponent, SevenElevenConfiguration, SevenElevenPaymentMethod>(
    componentClass = SevenElevenComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {

    override fun createComponent(
        delegate: EContextDelegate<SevenElevenPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<SevenElevenPaymentMethod>>,
    ): SevenElevenComponent {
        return SevenElevenComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    override fun createPaymentMethod(): SevenElevenPaymentMethod {
        return SevenElevenPaymentMethod()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return SevenElevenComponent.PAYMENT_METHOD_TYPES
    }
}
