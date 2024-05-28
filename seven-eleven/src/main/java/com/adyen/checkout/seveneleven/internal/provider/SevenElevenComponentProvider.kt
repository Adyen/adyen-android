/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/1/2023.
 */

package com.adyen.checkout.seveneleven.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.SevenElevenPaymentMethod
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenComponentState
import com.adyen.checkout.seveneleven.SevenElevenConfiguration
import com.adyen.checkout.seveneleven.getSevenElevenConfiguration
import com.adyen.checkout.seveneleven.toCheckoutConfiguration

class SevenElevenComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsManager: AnalyticsManager? = null,
) : EContextComponentProvider<
    SevenElevenComponent,
    SevenElevenConfiguration,
    SevenElevenPaymentMethod,
    SevenElevenComponentState,
    >(
    componentClass = SevenElevenComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsManager = analyticsManager,
) {

    override fun createComponentState(
        data: PaymentComponentData<SevenElevenPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = SevenElevenComponentState(data, isInputValid, isReady)

    override fun createComponent(
        delegate: EContextDelegate<SevenElevenPaymentMethod, SevenElevenComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<SevenElevenComponentState>,
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

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): SevenElevenConfiguration? {
        return checkoutConfiguration.getSevenElevenConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: SevenElevenConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return SevenElevenComponent.PAYMENT_METHOD_TYPES
    }
}
