/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/5/2023.
 */

package com.adyen.checkout.cashapppay

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod

class CashAppPayComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : StoredPaymentComponentProvider<CashAppPayComponent, CashAppPayConfiguration> {

    override fun <T> get(
        owner: T,
        paymentMethod: PaymentMethod,
        configuration: CashAppPayConfiguration
    ): CashAppPayComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, paymentMethod, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: CashAppPayConfiguration,
        defaultArgs: Bundle?
    ): CashAppPayComponent {
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val cashAppPayDelegate = DefaultCashAppPayDelegate(
                paymentMethod = paymentMethod,
                configuration = configuration,
            )
            CashAppPayComponent(
                savedStateHandle = savedStateHandle,
                cashAppPayDelegate = cashAppPayDelegate,
                configuration = configuration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory).get(CashAppPayComponent::class.java)
    }

    override fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration
    ): CashAppPayComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null)
    }

    override fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration,
        key: String?
    ): CashAppPayComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null, key)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration,
        defaultArgs: Bundle?
    ): CashAppPayComponent {
        return get(savedStateRegistryOwner, viewModelStoreOwner, storedPaymentMethod, configuration, defaultArgs, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration,
        defaultArgs: Bundle?,
        key: String?
    ): CashAppPayComponent {
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val cashAppPayDelegate = StoredCashAppPayDelegate(
                storedPaymentMethod = storedPaymentMethod,
            )
            CashAppPayComponent(
                savedStateHandle = savedStateHandle,
                cashAppPayDelegate = cashAppPayDelegate,
                configuration = configuration
            )
        }

        return if (key == null) {
            ViewModelProvider(viewModelStoreOwner, factory)[CashAppPayComponent::class.java]
        } else {
            ViewModelProvider(viewModelStoreOwner, factory)[key, CashAppPayComponent::class.java]
        }
    }
}
