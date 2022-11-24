/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.entercash

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.EntercashPaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.issuerlist.DefaultIssuerListDelegate
import com.adyen.checkout.issuerlist.IssuerListComponentParamsMapper

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class EntercashComponentProvider(
    parentConfiguration: Configuration? = null,
) : PaymentComponentProvider<EntercashComponent, EntercashConfiguration> {

    private val componentParamsMapper = IssuerListComponentParamsMapper(parentConfiguration)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: EntercashConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): EntercashComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val delegate = DefaultIssuerListDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod
                ) { EntercashPaymentMethod() }
                EntercashComponent(
                    savedStateHandle,
                    delegate,
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, EntercashComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return EntercashComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
