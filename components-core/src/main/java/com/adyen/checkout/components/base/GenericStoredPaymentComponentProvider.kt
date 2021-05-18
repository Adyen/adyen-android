/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/12/2020.
 */

package com.adyen.checkout.components.base

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod

class GenericStoredPaymentComponentProvider<
    BaseComponentT : BasePaymentComponent<*, *, *, *>,
    ConfigurationT : Configuration
    >(private val componentClass: Class<BaseComponentT>) : StoredPaymentComponentProvider<BaseComponentT, ConfigurationT> {

    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT {
        val genericStoredFactory: ViewModelProvider.Factory = viewModelFactory {
            componentClass.getConstructor(
                GenericStoredPaymentDelegate::class.java,
                configuration.javaClass
            ).newInstance(GenericStoredPaymentDelegate(storedPaymentMethod), configuration)
        }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[componentClass]
    }

    override fun get(
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
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[componentClass]
    }
}
