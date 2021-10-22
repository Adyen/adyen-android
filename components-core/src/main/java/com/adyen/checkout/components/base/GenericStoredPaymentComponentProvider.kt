/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/12/2020.
 */

package com.adyen.checkout.components.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod

class GenericStoredPaymentComponentProvider<
    BaseComponentT : BasePaymentComponent<*, *, *, *>,
    ConfigurationT : Configuration
    >(private val componentClass: Class<BaseComponentT>) : StoredPaymentComponentProvider<BaseComponentT, ConfigurationT> {

    override fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT where T : ViewModelStoreOwner, T : SavedStateRegistryOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        defaultArgs: Bundle?
    ): BaseComponentT {
        val genericStoredFactory: ViewModelProvider.Factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            componentClass.getConstructor(
                SavedStateHandle::class.java,
                GenericStoredPaymentDelegate::class.java,
                configuration.javaClass
            ).newInstance(savedStateHandle, GenericStoredPaymentDelegate(storedPaymentMethod), configuration)
        }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[componentClass]
    }

    override fun <T> get(
        owner: T,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT
    ): BaseComponentT where T : ViewModelStoreOwner, T : SavedStateRegistryOwner {
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
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[componentClass]
    }
}
