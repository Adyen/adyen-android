/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

internal class CardComponentParamsMapper(
    private val parentConfiguration: Configuration?
) {

    fun mapToParams(
        cardConfiguration: CardConfiguration,
        paymentMethod: PaymentMethod,
    ): CardComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: cardConfiguration,
            cardConfiguration = cardConfiguration,
            supportedCardTypes = getSupportedCardTypes(cardConfiguration, paymentMethod)
        )
    }

    fun mapToParams(
        cardConfiguration: CardConfiguration,
        // not needed for the actual mapping but indicates that this is the method to use in a stored flow
        storedPaymentMethod: StoredPaymentMethod,
    ): CardComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: cardConfiguration,
            cardConfiguration = cardConfiguration,
            supportedCardTypes = cardConfiguration.supportedCardTypes
        )
    }

    private fun mapToParams(
        parentConfiguration: Configuration,
        cardConfiguration: CardConfiguration,
        supportedCardTypes: List<CardType>,
    ): CardComponentParams {
        with(cardConfiguration) {
            return CardComponentParams(
                shopperLocale = parentConfiguration.shopperLocale,
                environment = parentConfiguration.environment,
                clientKey = parentConfiguration.clientKey,
                isHolderNameRequired = isHolderNameRequired,
                supportedCardTypes = supportedCardTypes,
                shopperReference = shopperReference,
                isStorePaymentFieldVisible = isStorePaymentFieldVisible,
                isHideCvc = isHideCvc,
                isHideCvcStoredCard = isHideCvcStoredCard,
                socialSecurityNumberVisibility = socialSecurityNumberVisibility,
                kcpAuthVisibility = kcpAuthVisibility,
                installmentConfiguration = installmentConfiguration,
                addressConfiguration = addressConfiguration
            )
        }
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     */
    private fun getSupportedCardTypes(
        cardConfiguration: CardConfiguration,
        paymentMethod: PaymentMethod
    ): List<CardType> {
        return when {
            cardConfiguration.supportedCardTypes.isNotEmpty() -> {
                Logger.v(TAG, "Reading supportedCardTypes from configuration")
                cardConfiguration.supportedCardTypes
            }
            paymentMethod.brands.orEmpty().isNotEmpty() -> {
                Logger.v(TAG, "Reading supportedCardTypes from API brands")
                paymentMethod.brands.orEmpty().map {
                    CardType.getByBrandName(it)
                }
            }
            else -> {
                Logger.v(TAG, "Falling back to CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST")
                CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
