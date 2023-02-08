/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.RestrictedCardType
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.ui.AddressFieldPolicy
import com.adyen.checkout.components.ui.AddressParams
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

internal class CardComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
) {

    fun mapToParamsDefault(
        cardConfiguration: CardConfiguration,
        paymentMethod: PaymentMethod,
        sessionSetupConfiguration: SessionSetupConfiguration? = null
    ): CardComponentParams {
        val supportedCardBrands = cardConfiguration.getSupportedCardBrands(paymentMethod)
        return cardConfiguration
            .mapToParamsInternal(supportedCardBrands, sessionSetupConfiguration)
            .override(overrideComponentParams)
    }

    fun mapToParamsStored(
        cardConfiguration: CardConfiguration,
    ): CardComponentParams {
        val supportedCardBrands = cardConfiguration.getSupportedCardBrandsStored()
        return cardConfiguration
            .mapToParamsInternal(supportedCardBrands)
            .override(overrideComponentParams)
    }

    private fun CardConfiguration.mapToParamsInternal(
        supportedCardBrands: List<CardBrand>,
        sessionSetupConfiguration: SessionSetupConfiguration? = null
    ): CardComponentParams {
        return CardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            isAnalyticsEnabled = isAnalyticsEnabled ?: true,
            isCreatedByDropIn = false,
            amount = amount,
            isHolderNameRequired = isHolderNameRequired ?: false,
            isSubmitButtonVisible = isSubmitButtonVisible ?: true,
            supportedCardBrands = supportedCardBrands,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = sessionSetupConfiguration?.enableStoreDetails
                ?: isStorePaymentFieldVisible ?: true,
            isHideCvc = isHideCvc ?: false,
            isHideCvcStoredCard = isHideCvcStoredCard ?: false,
            socialSecurityNumberVisibility = socialSecurityNumberVisibility ?: SocialSecurityNumberVisibility.HIDE,
            kcpAuthVisibility = kcpAuthVisibility ?: KCPAuthVisibility.HIDE,
            installmentConfiguration = installmentConfiguration,
            addressParams = addressConfiguration?.mapToAddressParam() ?: AddressParams.None
        )
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     * remove restricted card type
     */
    private fun CardConfiguration.getSupportedCardBrands(
        paymentMethod: PaymentMethod
    ): List<CardBrand> {
        return when {
            !supportedCardBrands.isNullOrEmpty() -> {
                Logger.v(TAG, "Reading supportedCardTypes from configuration")
                supportedCardBrands
            }
            paymentMethod.brands.orEmpty().isNotEmpty() -> {
                Logger.v(TAG, "Reading supportedCardTypes from API brands")
                paymentMethod.brands.orEmpty().map {
                    CardBrand(txVariant = it)
                }
            }
            else -> {
                Logger.v(TAG, "Falling back to CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST")
                CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST
            }
        }.removeRestrictedCards()
    }

    private fun CardConfiguration.getSupportedCardBrandsStored(): List<CardBrand> {
        return supportedCardBrands.orEmpty().removeRestrictedCards()
    }

    private fun List<CardBrand>.removeRestrictedCards(): List<CardBrand> {
        return this.filter { !RestrictedCardType.isRestrictedCardType(it.txVariant) }
    }

    private fun CardComponentParams.override(
        overrideComponentParams: ComponentParams?
    ): CardComponentParams {
        if (overrideComponentParams == null) return this
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            isAnalyticsEnabled = overrideComponentParams.isAnalyticsEnabled,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = overrideComponentParams.amount,
        )
    }

    private fun AddressConfiguration.mapToAddressParam(): AddressParams {
        return when (this) {
            is AddressConfiguration.FullAddress -> {
                AddressParams.FullAddress(
                    defaultCountryCode,
                    supportedCountryCodes,
                    addressFieldPolicy.mapToAddressParamFieldPolicy()
                )
            }
            AddressConfiguration.None -> {
                AddressParams.None
            }
            is AddressConfiguration.PostalCode -> {
                AddressParams.PostalCode(addressFieldPolicy.mapToAddressParamFieldPolicy())
            }
        }
    }

    private fun AddressConfiguration.CardAddressFieldPolicy.mapToAddressParamFieldPolicy(): AddressFieldPolicy {
        return when (this) {
            is AddressConfiguration.CardAddressFieldPolicy.Optional -> {
                AddressFieldPolicyParams.Optional
            }
            is AddressConfiguration.CardAddressFieldPolicy.OptionalForCardTypes -> {
                AddressFieldPolicyParams.OptionalForCardTypes(brands)
            }
            is AddressConfiguration.CardAddressFieldPolicy.Required -> {
                AddressFieldPolicyParams.Required
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
