/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.onlinebankingcore.OnlineBankingConfiguration

class OnlineBankingCZComponentProvider :
    PaymentComponentProvider<OnlineBankingCZComponent, OnlineBankingConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingConfiguration,
        defaultArgs: Bundle?
    ): OnlineBankingCZComponent {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate = DefaultOnlineBankingCZDelegate(paymentMethod) { OnlineBankingCZPaymentMethod() }

                OnlineBankingCZComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory).get(OnlineBankingCZComponent::class.java)
    }
}
