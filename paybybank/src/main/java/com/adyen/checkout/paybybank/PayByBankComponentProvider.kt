/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

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
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PayByBankComponentProvider(
    parentConfiguration: Configuration? = null,
) : PaymentComponentProvider<PayByBankComponent, PayByBankConfiguration> {

    private val componentParamsMapper = GenericComponentParamsMapper(parentConfiguration)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: PayByBankConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): PayByBankComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                PayByBankComponent(
                    savedStateHandle,
                    DefaultPayByBankDelegate(
                        PaymentObserverRepository(),
                        paymentMethod,
                        configuration,
                        componentParams,
                    ),
                    configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, PayByBankComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return PayByBankComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
