/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/1/2023.
 */

package com.adyen.checkout.conveniencestoresjp.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.ConvenienceStoresJPPaymentMethod
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPConfiguration
import com.adyen.checkout.econtext.EContextComponentProvider
import com.adyen.checkout.econtext.EContextDelegate
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ConvenienceStoresJPComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : EContextComponentProvider<
    ConvenienceStoresJPComponent,
    ConvenienceStoresJPConfiguration,
    ConvenienceStoresJPPaymentMethod>(
    componentClass = ConvenienceStoresJPComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {

    override fun createComponent(
        delegate: EContextDelegate<ConvenienceStoresJPPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<ConvenienceStoresJPPaymentMethod>>
    ): ConvenienceStoresJPComponent {
        return ConvenienceStoresJPComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler
        )
    }

    override fun createPaymentMethod(): ConvenienceStoresJPPaymentMethod {
        return ConvenienceStoresJPPaymentMethod()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return ConvenienceStoresJPComponent.PAYMENT_METHOD_TYPES
    }
}
