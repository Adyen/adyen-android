/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.bacs

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException

class BacsDirectDebitComponentProvider(
    parentConfiguration: Configuration? = null,
    isCreatedByDropIn: Boolean = false,
) : PaymentComponentProvider<BacsDirectDebitComponent, BacsDirectDebitConfiguration> {

    private val componentParamsMapper = BacsDirectDebitComponentParamsMapper(parentConfiguration, isCreatedByDropIn)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BacsDirectDebitConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): BacsDirectDebitComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                BacsDirectDebitComponent(
                    savedStateHandle,
                    DefaultBacsDirectDebitDelegate(
                        PaymentObserverRepository(),
                        componentParams,
                        paymentMethod
                    ),
                    configuration
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BacsDirectDebitComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BacsDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
