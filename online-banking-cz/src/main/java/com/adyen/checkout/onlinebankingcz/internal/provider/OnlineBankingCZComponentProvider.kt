/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingCZPaymentMethod
import com.adyen.checkout.onlinebankingcore.internal.provider.OnlineBankingComponentProvider
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponent
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZComponentState
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingcz.getOnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingcz.toCheckoutConfiguration

class OnlineBankingCZComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : OnlineBankingComponentProvider<
    OnlineBankingCZComponent,
    OnlineBankingCZConfiguration,
    OnlineBankingCZPaymentMethod,
    OnlineBankingCZComponentState,
    >(
    componentClass = OnlineBankingCZComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
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

    override fun createComponentState(
        data: PaymentComponentData<OnlineBankingCZPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OnlineBankingCZComponentState(data, isInputValid, isReady)

    override fun createComponent(
        delegate: OnlineBankingDelegate<OnlineBankingCZPaymentMethod, OnlineBankingCZComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OnlineBankingCZComponentState>
    ): OnlineBankingCZComponent {
        return OnlineBankingCZComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): OnlineBankingCZConfiguration? {
        return checkoutConfiguration.getOnlineBankingCZConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: OnlineBankingCZConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }
}
