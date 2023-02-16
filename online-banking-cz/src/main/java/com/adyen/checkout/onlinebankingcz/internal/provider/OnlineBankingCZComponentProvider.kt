/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.onlinebankingcore.internal.provider.OnlineBankingComponentProvider
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingCZComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : OnlineBankingComponentProvider<
    OnlineBankingCZComponent,
    OnlineBankingCZConfiguration,
    OnlineBankingCZPaymentMethod>(
    componentClass = OnlineBankingCZComponent::class.java,
    overrideComponentParams = overrideComponentParams
) {

    override fun createPaymentMethod(): OnlineBankingCZPaymentMethod {
        return OnlineBankingCZPaymentMethod()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return OnlineBankingCZComponent.PAYMENT_METHOD_TYPES
    }

    override fun getTermsAndConditionsUrl(): String {
        return OnlineBankingCZComponent.TERMS_CONDITIONS_URL
    }

    override fun createComponent(
        delegate: OnlineBankingDelegate<OnlineBankingCZPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<OnlineBankingCZPaymentMethod>>
    ): OnlineBankingCZComponent {
        return OnlineBankingCZComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler
        )
    }
}
