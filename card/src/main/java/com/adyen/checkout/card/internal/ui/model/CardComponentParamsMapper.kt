/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.model.AddressFieldPolicy
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

@Suppress("TooManyFunctions")
internal class CardComponentParamsMapper(
    private val installmentsParamsMapper: InstallmentsParamsMapper,
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParamsDefault(
        cardConfiguration: CardConfiguration,
        paymentMethod: PaymentMethod,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        val supportedCardBrands = cardConfiguration.getSupportedCardBrands(paymentMethod)
        return mapToParams(
            cardConfiguration,
            supportedCardBrands,
            sessionParams,
        )
    }

    fun mapToParamsStored(
        cardConfiguration: CardConfiguration,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        val supportedCardBrands = cardConfiguration.getSupportedCardBrandsStored()
        return mapToParams(
            cardConfiguration,
            supportedCardBrands,
            sessionParams,
        )
    }

    private fun mapToParams(
        cardConfiguration: CardConfiguration,
        supportedCardBrands: List<CardBrand>,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        return cardConfiguration
            .mapToParamsInternal(supportedCardBrands)
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CardConfiguration.mapToParamsInternal(
        supportedCardBrands: List<CardBrand>,
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
            isStorePaymentFieldVisible = isStorePaymentFieldVisible ?: true,
            isHideCvc = isHideCvc ?: false,
            isHideCvcStoredCard = isHideCvcStoredCard ?: false,
            socialSecurityNumberVisibility = socialSecurityNumberVisibility ?: SocialSecurityNumberVisibility.HIDE,
            kcpAuthVisibility = kcpAuthVisibility ?: KCPAuthVisibility.HIDE,
            installmentParams = installmentConfiguration?.let { installmentsParamsMapper.mapToInstallmentParams(it) },
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

    private fun CardComponentParams.override(
        sessionParams: SessionParams? = null
    ): CardComponentParams {
        if (sessionParams == null) return this
        return copy(
            isStorePaymentFieldVisible = sessionParams.enableStoreDetails ?: isStorePaymentFieldVisible,
            installmentParams = sessionParams.installmentOptions?.let {
                installmentsParamsMapper.mapToInstallmentParams(it)
            } ?: installmentParams,
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
