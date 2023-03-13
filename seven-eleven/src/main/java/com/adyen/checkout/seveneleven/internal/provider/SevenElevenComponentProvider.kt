/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/1/2023.
 */

package com.adyen.checkout.seveneleven.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.SevenElevenPaymentMethod
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.seveneleven.SevenElevenComponent
import com.adyen.checkout.seveneleven.SevenElevenComponentState
import com.adyen.checkout.seveneleven.SevenElevenConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SevenElevenComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : EContextComponentProvider<
    SevenElevenComponent,
    SevenElevenConfiguration,
    SevenElevenPaymentMethod,
    SevenElevenComponentState>(
    componentClass = SevenElevenComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
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

    override fun getSupportedPaymentMethods(): List<String> {
        return SevenElevenComponent.PAYMENT_METHOD_TYPES
    }
}
