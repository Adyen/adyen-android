/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.components

import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException

interface StoredPaymentComponentProvider<ComponentT : PaymentComponent<*, *>, ConfigurationT : Configuration> :
    PaymentComponentProvider<ComponentT, ConfigurationT> {
    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param owner               The Activity or Fragment to associate the lifecycle.
     * @param storedPaymentMethod The corresponding  [StoredPaymentMethod] object.
     * @param configuration       The Configuration of the component.
     * @return The Component
     */
    @Throws(CheckoutException::class)
    operator fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT
    ): ComponentT where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null)
    }

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param storedPaymentMethod     The corresponding  [StoredPaymentMethod] object.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this `Bundle` will be used as defaults by [SavedStateHandle] passed in [ViewModel]
     *                                if there is no previously saved state or previously saved state misses a value by such key
     * @return The Component
     */
    @Throws(CheckoutException::class)
    operator fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        defaultArgs: Bundle?
    ): ComponentT
}
