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
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.components.repository.ObserverRepository
import com.adyen.checkout.core.exception.ComponentException
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
        defaultArgs: Bundle?,
        key: String?,
    ): OnlineBankingComponent<OnlineBankingSKPaymentMethod> {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val delegate =
                    DefaultOnlineBankingDelegate(
                        observerRepository = ObserverRepository(),
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
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingSKComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return OnlineBankingSKComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
