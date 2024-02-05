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
import com.adyen.checkout.card.getCardConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.model.AddressFieldPolicy
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

@Suppress("TooManyFunctions")
internal class CardComponentParamsMapper(
    private val installmentsParamsMapper: InstallmentsParamsMapper,
    private val dropInOverrideParams: DropInOverrideParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParamsDefault(
        checkoutConfiguration: CheckoutConfiguration,
        paymentMethod: PaymentMethod,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        return mapToParams(
            checkoutConfiguration,
            paymentMethod,
            sessionParams,
        )
    }

    fun mapToParamsStored(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        return mapToParams(
            checkoutConfiguration,
            null,
            sessionParams,
        )
    }

    private fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        paymentMethod: PaymentMethod?,
        sessionParams: SessionParams?,
    ): CardComponentParams {
        return checkoutConfiguration
            .mapToParamsInternal(paymentMethod)
            .override(dropInOverrideParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun CheckoutConfiguration.mapToParamsInternal(
        paymentMethod: PaymentMethod?,
    ): CardComponentParams {
        val cardConfiguration = getCardConfiguration()
        return CardComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            amount = amount,
            isHolderNameRequired = cardConfiguration?.isHolderNameRequired ?: false,
            isSubmitButtonVisible = cardConfiguration?.isSubmitButtonVisible ?: true,
            supportedCardBrands = getSupportedCardBrands(paymentMethod),
            shopperReference = cardConfiguration?.shopperReference,
            isStorePaymentFieldVisible = cardConfiguration?.isStorePaymentFieldVisible ?: true,
            socialSecurityNumberVisibility = cardConfiguration?.socialSecurityNumberVisibility
                ?: SocialSecurityNumberVisibility.HIDE,
            kcpAuthVisibility = cardConfiguration?.kcpAuthVisibility ?: KCPAuthVisibility.HIDE,
            installmentParams = installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = cardConfiguration?.installmentConfiguration,
                amount = amount,
                shopperLocale = shopperLocale,
            ),
            addressParams = cardConfiguration?.addressConfiguration?.mapToAddressParam() ?: AddressParams.None,
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
    private fun CheckoutConfiguration.getSupportedCardBrands(
        paymentMethod: PaymentMethod?
    ): List<CardBrand> {
        val supportedCardBrands = getCardConfiguration()?.supportedCardBrands
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
                CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST
            }
        }.removeRestrictedCards()
    }

    private fun List<CardBrand>.removeRestrictedCards(): List<CardBrand> {
        return this.filter { !RestrictedCardType.isRestrictedCardType(it.txVariant) }
    }

    private fun AddressConfiguration.mapToAddressParam(): AddressParams {
        return when (this) {
            is AddressConfiguration.FullAddress -> {
                AddressParams.FullAddress(
                    defaultCountryCode,
                    supportedCountryCodes,
                    addressFieldPolicy.mapToAddressParamFieldPolicy(),
                )
            }

            AddressConfiguration.None -> {
                AddressParams.None
            }

            is AddressConfiguration.PostalCode -> {
                AddressParams.PostalCode(addressFieldPolicy.mapToAddressParamFieldPolicy())
            }

            is AddressConfiguration.Lookup -> {
                AddressParams.Lookup()
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
        dropInOverrideParams: DropInOverrideParams?
    ): CardComponentParams {
        if (dropInOverrideParams == null) return this
        return copy(
            amount = dropInOverrideParams.amount,
            isCreatedByDropIn = true,
        )
    }

    private fun CardComponentParams.override(
        sessionParams: SessionParams?
    ): CardComponentParams {
        if (sessionParams == null) return this
        return copy(
            isStorePaymentFieldVisible = sessionParams.enableStoreDetails ?: isStorePaymentFieldVisible,
            // we don't fall back to the original value of installmentParams value on purpose
            // if sessionParams.installmentOptions is null we want installmentParams to be also null regardless of what
            // InstallmentConfiguration is passed to the mapper
            installmentParams = installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = sessionParams.installmentConfiguration,
                amount = sessionParams.amount ?: amount,
                shopperLocale = shopperLocale,
            ),
            amount = sessionParams.amount ?: amount,
        )
    }
}
