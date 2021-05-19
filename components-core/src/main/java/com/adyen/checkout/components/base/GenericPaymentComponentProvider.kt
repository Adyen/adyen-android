/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */
package com.adyen.checkout.components.base

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

class GenericPaymentComponentProvider<BaseComponentT : BasePaymentComponent<*, *, *, *>, ConfigurationT : Configuration>(
    private val componentClass: Class<BaseComponentT>
) : PaymentComponentProvider<BaseComponentT, ConfigurationT> {

    override operator fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT {
        val genericFactory: ViewModelProvider.Factory = viewModelFactory {
            componentClass.getConstructor(
                GenericPaymentMethodDelegate::class.java,
                configuration.javaClass
            ).newInstance(GenericPaymentMethodDelegate(paymentMethod), configuration)
        }
        return ViewModelProvider(viewModelStoreOwner, genericFactory).get(componentClass)
    }
}
