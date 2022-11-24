/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BcmcComponentProvider(
    parentConfiguration: Configuration? = null,
) : PaymentComponentProvider<BcmcComponent, BcmcConfiguration> {

    private val componentParamsMapper = BcmcComponentParamsMapper(parentConfiguration)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): BcmcComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration)
        val publicKeyRepository = DefaultPublicKeyRepository()
        val cardValidationMapper = CardValidationMapper()
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            BcmcComponent(
                savedStateHandle = savedStateHandle,
                delegate = DefaultBcmcDelegate(
                    observerRepository = PaymentObserverRepository(),
                    paymentMethod = paymentMethod,
                    publicKeyRepository = publicKeyRepository,
                    componentParams = componentParams,
                    cardValidationMapper = cardValidationMapper,
                    cardEncrypter = cardEncrypter
                ),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory)[key, BcmcComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BcmcComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
