/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */
package com.adyen.checkout.components.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

class GenericPaymentComponentProvider<BaseComponentT : BasePaymentComponent<*, *, *, *>, ConfigurationT : Configuration>(
    private val componentClass: Class<BaseComponentT>
) : PaymentComponentProvider<BaseComponentT, ConfigurationT> {

    override fun <T> get(
        owner: T,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, paymentMethod, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        defaultArgs: Bundle?
    ): BaseComponentT {
        val genericFactory: ViewModelProvider.Factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            componentClass.getConstructor(
                SavedStateHandle::class.java,
                GenericPaymentMethodDelegate::class.java,
                configuration.javaClass
            ).newInstance(savedStateHandle, GenericPaymentMethodDelegate(paymentMethod), configuration)
        }
        return ViewModelProvider(viewModelStoreOwner, genericFactory).get(componentClass)
    }
}
