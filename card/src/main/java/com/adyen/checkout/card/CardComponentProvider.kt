/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.card

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

class CardComponentProvider : StoredPaymentComponentProvider<CardComponent, CardConfiguration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration
    ): CardComponent {
        val verifiedConfiguration = checkSupportedCardTypes(paymentMethod, configuration)
        val factory = viewModelFactory { CardComponent(NewCardDelegate(paymentMethod, verifiedConfiguration), verifiedConfiguration) }
        return ViewModelProvider(viewModelStoreOwner, factory).get(CardComponent::class.java)
    }

    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration
    ): CardComponent {
        val factory = viewModelFactory { CardComponent(StoredCardDelegate(storedPaymentMethod, configuration), configuration) }
        return ViewModelProvider(viewModelStoreOwner, factory).get(CardComponent::class.java)
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        callback: ComponentAvailableCallback<CardConfiguration?>
    ) {
        callback.onAvailabilityResult(true, paymentMethod, configuration)
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
                if (brandType != null) {
                    supportedCardTypes.add(brandType)
                } else {
                    Logger.e(TAG, "Failed to get card type for brand: $brand")
                }
            }
        } else {
            Logger.d(TAG, "Falling back to DEFAULT_SUPPORTED_CARDS_LIST")
        }
        @Suppress("SpreadOperator")
        return cardConfiguration.newBuilder()
            .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
            .build()
    }
}
