/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.card

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.repository.DefaultAddressRepository
import com.adyen.checkout.card.repository.DefaultDetectCardTypeRepository
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter

private val TAG = LogUtil.getTag()

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardComponentProvider : StoredPaymentComponentProvider<CardComponent, CardConfiguration> {

    private val componentParamsMapper = CardComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): CardComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration, paymentMethod)
        val verifiedConfiguration = checkSupportedCardTypes(paymentMethod, configuration)
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncrypter)
        val publicKeyRepository = DefaultPublicKeyRepository()
        val addressRepository = DefaultAddressRepository()
        val cardValidationMapper = CardValidationMapper()
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            CardComponent(
                savedStateHandle,
                DefaultCardDelegate(
                    observerRepository = PaymentObserverRepository(),
                    publicKeyRepository = publicKeyRepository,
                    configuration = verifiedConfiguration,
                    paymentMethod = paymentMethod,
                    addressRepository = addressRepository,
                    detectCardTypeRepository = detectCardTypeRepository,
                    cardValidationMapper = cardValidationMapper,
                    cardEncrypter = cardEncrypter,
                    genericEncrypter = genericEncrypter
                ),
                verifiedConfiguration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java]
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration, storedPaymentMethod)
        val publicKeyRepository = DefaultPublicKeyRepository()
        val genericEncrypter = DefaultGenericEncrypter()
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            CardComponent(
                savedStateHandle,
                StoredCardDelegate(
                    observerRepository = PaymentObserverRepository(),
                    storedPaymentMethod = storedPaymentMethod,
                    configuration = configuration,
                    cardEncrypter = cardEncrypter,
                    publicKeyRepository = publicKeyRepository,
                ),
                configuration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java]
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     *
     * @param paymentMethod The payment methods object that will start the component.
     * @param cardConfiguration The configuration object that will start the component.
     * @return The Configuration object with possibly adjusted values.
     */
    // TODO remove after replacing configuration with params
    private fun checkSupportedCardTypes(
        paymentMethod: PaymentMethod,
        cardConfiguration: CardConfiguration
    ): CardConfiguration {
        if (cardConfiguration.supportedCardTypes.isNotEmpty()) {
            return cardConfiguration
        }

        val brands = paymentMethod.brands
        var supportedCardTypes = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST

        // Get card types from brands in PaymentMethod object
        if (!brands.isNullOrEmpty()) {
            supportedCardTypes = arrayListOf()
            for (brand in brands) {
                val brandType = CardType.getByBrandName(brand)
                supportedCardTypes.add(brandType)
            }
        } else {
            Logger.d(TAG, "Falling back to DEFAULT_SUPPORTED_CARDS_LIST")
        }
        @Suppress("SpreadOperator")
        return cardConfiguration.newBuilder()
            .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
            .build()
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
