/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.card

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionComponent
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.card.repository.DefaultAddressRepository
import com.adyen.checkout.card.repository.DefaultDetectCardTypeRepository
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardComponentProvider(
    parentConfiguration: Configuration? = null,
) : StoredPaymentComponentProvider<CardComponent, CardConfiguration> {

    private val componentParamsMapper = CardComponentParamsMapper(parentConfiguration)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): CardComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration, paymentMethod)
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncrypter)
        val publicKeyRepository = DefaultPublicKeyRepository()
        val addressRepository = DefaultAddressRepository()
        val cardValidationMapper = CardValidationMapper()

        val actionConfiguration = GenericActionConfiguration.Builder(
            configuration.shopperLocale,
            configuration.environment,
            configuration.clientKey
        ).build()

        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val genericActionDelegate = GenericActionComponent.PROVIDER.getDelegate(
                actionConfiguration,
                savedStateHandle,
                application,
            )

            CardComponent(
                savedStateHandle = savedStateHandle,
                delegate = DefaultCardDelegate(
                    observerRepository = PaymentObserverRepository(),
                    publicKeyRepository = publicKeyRepository,
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    addressRepository = addressRepository,
                    detectCardTypeRepository = detectCardTypeRepository,
                    cardValidationMapper = cardValidationMapper,
                    cardEncrypter = cardEncrypter,
                    genericEncrypter = genericEncrypter,
                ),
                cardConfiguration = configuration,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java]
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration, storedPaymentMethod)
        val publicKeyRepository = DefaultPublicKeyRepository()
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)

        val actionConfiguration = GenericActionConfiguration.Builder(
            configuration.shopperLocale,
            configuration.environment,
            configuration.clientKey
        ).build()

        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val genericActionDelegate = GenericActionComponent.PROVIDER.getDelegate(
                actionConfiguration,
                savedStateHandle,
                application,
            )

            CardComponent(
                savedStateHandle = savedStateHandle,
                delegate = StoredCardDelegate(
                    observerRepository = PaymentObserverRepository(),
                    storedPaymentMethod = storedPaymentMethod,
                    componentParams = componentParams,
                    cardEncrypter = cardEncrypter,
                    publicKeyRepository = publicKeyRepository,
                ),
                cardConfiguration = configuration,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    private fun assertSupported(storedPaymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(storedPaymentMethod)) {
            throw ComponentException("Unsupported payment method ${storedPaymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return CardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return CardComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
