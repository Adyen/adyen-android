/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.PdfOpener

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingCZComponentProvider(
    parentConfiguration: Configuration? = null,
) : PaymentComponentProvider<OnlineBankingComponent<OnlineBankingCZPaymentMethod>, OnlineBankingCZConfiguration> {

    private val componentParamsMapper = GenericComponentParamsMapper(parentConfiguration)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingCZConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): OnlineBankingComponent<OnlineBankingCZPaymentMethod> {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val delegate =
                    DefaultOnlineBankingDelegate(
                        observerRepository = PaymentObserverRepository(),
                        pdfOpener = PdfOpener(),
                        paymentMethod = paymentMethod,
                        componentParams = componentParams,
                        termsAndConditionsUrl = OnlineBankingCZComponent.TERMS_CONDITIONS_URL
                    ) { OnlineBankingCZPaymentMethod() }

                OnlineBankingCZComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingCZComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return OnlineBankingCZComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
