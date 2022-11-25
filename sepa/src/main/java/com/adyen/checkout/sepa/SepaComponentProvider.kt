/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.sepa

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
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SepaComponentProvider(
    parentConfiguration: Configuration? = null,
    isCreatedByDropIn: Boolean = false,
) : PaymentComponentProvider<SepaComponent, SepaConfiguration> {

    private val componentParamsMapper = GenericComponentParamsMapper(parentConfiguration, isCreatedByDropIn)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: SepaConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): SepaComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                SepaComponent(
                    savedStateHandle,
                    DefaultSepaDelegate(PaymentObserverRepository(), componentParams, paymentMethod),
                    configuration,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, SepaComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return SepaComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
