/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.onlinebankingjp.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.OnlineBankingJPPaymentMethod
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponent
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPConfiguration
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingJPComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : EContextComponentProvider<
    OnlineBankingJPComponent,
    OnlineBankingJPConfiguration,
    OnlineBankingJPPaymentMethod>(
    componentClass = OnlineBankingJPComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {

    override fun createComponent(
        delegate: EContextDelegate<OnlineBankingJPPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<OnlineBankingJPPaymentMethod>>
    ): OnlineBankingJPComponent {
        return OnlineBankingJPComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    override fun createPaymentMethod(): OnlineBankingJPPaymentMethod {
        return OnlineBankingJPPaymentMethod()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return OnlineBankingJPComponent.PAYMENT_METHOD_TYPES
    }
}
