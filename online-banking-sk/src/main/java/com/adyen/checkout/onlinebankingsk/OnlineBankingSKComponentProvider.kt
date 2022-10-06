/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.PdfOpener

class OnlineBankingSKComponentProvider :
    PaymentComponentProvider<OnlineBankingComponent<OnlineBankingSKPaymentMethod>, OnlineBankingSKConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingSKConfiguration,
        defaultArgs: Bundle?
    ): OnlineBankingComponent<OnlineBankingSKPaymentMethod> {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate =
                    DefaultOnlineBankingDelegate(
                        pdfOpener = PdfOpener(),
                        paymentMethod = paymentMethod,
                        configuration = configuration,
                        termsAndConditionsUrl = OnlineBankingSKComponent.TERMS_CONDITIONS_URL
                    ) { OnlineBankingSKPaymentMethod() }

                OnlineBankingSKComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[OnlineBankingSKComponent::class.java]
    }
}
