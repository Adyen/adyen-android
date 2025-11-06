/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlin.collections.isNullOrEmpty

// TODO - Card Component Mapper Tests.
internal class CardComponentParamsMapper {

    fun mapToParams(
        componentParamsBundle: ComponentParamsBundle,
        cardConfiguration: CardConfiguration?,
        paymentMethod: PaymentMethod?,
    ): CardComponentParams {
        val (commonComponentParams, sessionParams) = componentParamsBundle
        return CardComponentParams(
            commonComponentParams = commonComponentParams,
            isHolderNameRequired = cardConfiguration?.isHolderNameRequired ?: false,
            supportedCardBrands = getSupportedCardBrands(cardConfiguration, paymentMethod),
            shopperReference = cardConfiguration?.shopperReference,
            isStorePaymentFieldVisible = getStorePaymentFieldVisible(sessionParams, cardConfiguration),
            socialSecurityNumberVisibility = cardConfiguration?.socialSecurityNumberVisibility
                ?: SocialSecurityNumberVisibility.HIDE,
            kcpAuthVisibility = cardConfiguration?.kcpAuthVisibility ?: KCPAuthVisibility.HIDE,
            cvcVisibility = if (cardConfiguration?.isHideCvc == true) {
                CVCVisibility.ALWAYS_HIDE
            } else {
                CVCVisibility.ALWAYS_SHOW
            },
            storedCVCVisibility = if (cardConfiguration?.isHideCvcStoredCard == true) {
                StoredCVCVisibility.HIDE
            } else {
                StoredCVCVisibility.SHOW
            },
        )
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     * remove restricted card type
     */
    private fun getSupportedCardBrands(
        cardConfiguration: CardConfiguration?,
        paymentMethod: PaymentMethod?
    ): List<CardBrand> {
        val supportedCardBrands = cardConfiguration?.supportedCardBrands
        return when {
            !supportedCardBrands.isNullOrEmpty() -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Reading supportedCardTypes from configuration" }
                supportedCardBrands
            }

            paymentMethod?.brands.orEmpty().isNotEmpty() -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Reading supportedCardTypes from API brands" }
                paymentMethod?.brands.orEmpty().map {
                    CardBrand(txVariant = it)
                }
            }

            else -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Falling back to CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST" }
                DEFAULT_SUPPORTED_CARDS_LIST
            }
        }.removeRestrictedCards()
    }

    private fun List<CardBrand>.removeRestrictedCards(): List<CardBrand> {
        return this.filter { !RestrictedCardType.isRestrictedCardType(it.txVariant) }
    }

    private fun getStorePaymentFieldVisible(
        sessionParams: SessionParams?,
        cardConfiguration: CardConfiguration?,
    ): Boolean {
        return sessionParams?.enableStoreDetails ?: cardConfiguration?.isStorePaymentFieldVisible ?: true
    }

    companion object {
        val DEFAULT_SUPPORTED_CARDS_LIST: List<CardBrand> = listOf(
            CardBrand(CardType.VISA.txVariant),
            CardBrand(CardType.AMERICAN_EXPRESS.txVariant),
            CardBrand(CardType.MASTERCARD.txVariant),
        )
    }
}
