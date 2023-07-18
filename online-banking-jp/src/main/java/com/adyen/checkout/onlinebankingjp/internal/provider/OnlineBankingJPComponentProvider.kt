/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.onlinebankingjp.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingJPPaymentMethod
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponent
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPComponentState
import com.adyen.checkout.onlinebankingjp.OnlineBankingJPConfiguration

class OnlineBankingJPComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : EContextComponentProvider<
    OnlineBankingJPComponent,
    OnlineBankingJPConfiguration,
    OnlineBankingJPPaymentMethod,
    OnlineBankingJPComponentState
    >(
    componentClass = OnlineBankingJPComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponentState(
        data: PaymentComponentData<OnlineBankingJPPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OnlineBankingJPComponentState(data, isInputValid, isReady)

    override fun createComponent(
        delegate: EContextDelegate<OnlineBankingJPPaymentMethod, OnlineBankingJPComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OnlineBankingJPComponentState>
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
