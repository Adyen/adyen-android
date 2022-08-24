/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.blik

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod

class BlikComponentProvider : StoredPaymentComponentProvider<BlikComponent, BlikConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BlikConfiguration,
        defaultArgs: Bundle?
    ): BlikComponent {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                BlikComponent(
                    savedStateHandle = savedStateHandle,
                    blikDelegate = DefaultBlikDelegate(paymentMethod),
                    configuration = configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[BlikComponent::class.java]
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: BlikConfiguration,
        defaultArgs: Bundle?
    ): BlikComponent {
        val genericStoredFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                BlikComponent(
                    savedStateHandle = savedStateHandle,
                    blikDelegate = StoredBlikDelegate(storedPaymentMethod),
                    configuration = configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[BlikComponent::class.java]
    }
}
