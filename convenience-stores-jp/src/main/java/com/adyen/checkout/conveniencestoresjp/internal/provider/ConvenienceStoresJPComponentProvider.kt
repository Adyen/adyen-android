/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/1/2023.
 */

package com.adyen.checkout.conveniencestoresjp.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.paymentmethod.ConvenienceStoresJPPaymentMethod
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponent
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPComponentState
import com.adyen.checkout.conveniencestoresjp.ConvenienceStoresJPConfiguration
import com.adyen.checkout.conveniencestoresjp.getConvenienceStoresJPConfiguration
import com.adyen.checkout.conveniencestoresjp.toCheckoutConfiguration
import com.adyen.checkout.econtext.internal.provider.EContextComponentProvider
import com.adyen.checkout.econtext.internal.ui.EContextDelegate

class ConvenienceStoresJPComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    dropInOverrideParams: DropInOverrideParams? = null,
    analyticsRepository: AnalyticsRepository? = null,
) : EContextComponentProvider<
    ConvenienceStoresJPComponent,
    ConvenienceStoresJPConfiguration,
    ConvenienceStoresJPPaymentMethod,
    ConvenienceStoresJPComponentState,
    >(
    componentClass = ConvenienceStoresJPComponent::class.java,
    dropInOverrideParams = dropInOverrideParams,
    analyticsRepository = analyticsRepository,
) {

    override fun createComponentState(
        data: PaymentComponentData<ConvenienceStoresJPPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = ConvenienceStoresJPComponentState(data, isInputValid, isReady)

    override fun createComponent(
        delegate: EContextDelegate<ConvenienceStoresJPPaymentMethod, ConvenienceStoresJPComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<ConvenienceStoresJPComponentState>
    ): ConvenienceStoresJPComponent {
        return ConvenienceStoresJPComponent(
            delegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    override fun createPaymentMethod(): ConvenienceStoresJPPaymentMethod {
        return ConvenienceStoresJPPaymentMethod()
    }

    override fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): ConvenienceStoresJPConfiguration? {
        return checkoutConfiguration.getConvenienceStoresJPConfiguration()
    }

    override fun getCheckoutConfiguration(configuration: ConvenienceStoresJPConfiguration): CheckoutConfiguration {
        return configuration.toCheckoutConfiguration()
    }

    override fun getSupportedPaymentMethods(): List<String> {
        return ConvenienceStoresJPComponent.PAYMENT_METHOD_TYPES
    }
}
