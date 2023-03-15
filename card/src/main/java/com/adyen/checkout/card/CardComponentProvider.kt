/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.card

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.repository.BinLookupRepository
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

class CardComponentProvider : StoredPaymentComponentProvider<CardComponent, CardConfiguration> {

    override fun <T> get(
        owner: T,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration
    ): CardComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, paymentMethod, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        defaultArgs: Bundle?
    ): CardComponent {
        val verifiedConfiguration = checkSupportedCardTypes(paymentMethod, configuration)
        val binLookupRepository = BinLookupRepository()
        val publicKeyRepository = PublicKeyRepository()
        val addressDelegate = AddressDelegate(AddressRepository())
        val cardValidationMapper = CardValidationMapper()
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            CardComponent(
                savedStateHandle,
                NewCardDelegate(
                    paymentMethod,
                    verifiedConfiguration,
                    binLookupRepository,
                    publicKeyRepository,
                    addressDelegate,
                    cardValidationMapper
                ),
                verifiedConfiguration
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory).get(CardComponent::class.java)
    }

    override fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration
    ): CardComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null)
    }

    override fun <T> get(
        owner: T,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        key: String?
    ): CardComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, storedPaymentMethod, configuration, null, key)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        defaultArgs: Bundle?
    ): CardComponent {
        return get(savedStateRegistryOwner, viewModelStoreOwner, storedPaymentMethod, configuration, defaultArgs, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        defaultArgs: Bundle?,
        key: String?
    ): CardComponent {
        val publicKeyRepository = PublicKeyRepository()
        val factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            CardComponent(
                savedStateHandle,
                StoredCardDelegate(
                    storedPaymentMethod,
                    configuration,
                    publicKeyRepository
                ),
                configuration
            )
        }

        return if (key == null) {
            ViewModelProvider(viewModelStoreOwner, factory)[CardComponent::class.java]
        } else {
            ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java]
        }
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     *
     * @param paymentMethod The payment methods object that will start the component.
     * @param cardConfiguration The configuration object that will start the component.
     * @return The Configuration object with possibly adjusted values.
     */
    private fun checkSupportedCardTypes(paymentMethod: PaymentMethod, cardConfiguration: CardConfiguration): CardConfiguration {
        if (cardConfiguration.supportedCardBrands.isNotEmpty()) {
            return cardConfiguration
        }

        val brands = paymentMethod.brands
        var supportedCardBrands = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST.map {
            CardBrand(it)
        }

        // Get card brands from brands in PaymentMethod object
        if (!brands.isNullOrEmpty()) {
            supportedCardBrands = arrayListOf()
            for (brand in brands) {
                val cardBrand = CardBrand(brand)
                supportedCardBrands.add(cardBrand)
            }
        } else {
            Logger.d(TAG, "Falling back to DEFAULT_SUPPORTED_CARDS_LIST")
        }
        @Suppress("SpreadOperator")
        return cardConfiguration.newBuilder()
            .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
            .build()
    }
}
