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
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.PdfOpener

class OnlineBankingCZComponentProvider :
    PaymentComponentProvider<OnlineBankingComponent<OnlineBankingCZPaymentMethod>, OnlineBankingCZConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingCZConfiguration,
        defaultArgs: Bundle?
    ): OnlineBankingComponent<OnlineBankingCZPaymentMethod> {
        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate =
                    DefaultOnlineBankingDelegate(
                        pdfOpener = PdfOpener(),
                        paymentMethod = paymentMethod,
                        configuration = configuration,
                        termsAndConditionsUrl = OnlineBankingCZComponent.TERMS_CONDITIONS_URL
                    ) { OnlineBankingCZPaymentMethod() }

                OnlineBankingCZComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[OnlineBankingCZComponent::class.java]
    }
}
