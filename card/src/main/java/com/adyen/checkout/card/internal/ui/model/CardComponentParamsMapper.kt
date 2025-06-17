/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.getCardConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.model.AddressFieldPolicy
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import java.util.Locale

@Suppress("TooManyFunctions")
internal class CardComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
    private val installmentsParamsMapper: InstallmentsParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
    ): CardComponentParams {
        return mapToParamsInternal(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
            paymentMethod,
        )
    }

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        @Suppress("UNUSED_PARAMETER") storedPaymentMethod: StoredPaymentMethod,
    ): CardComponentParams {
        return mapToParamsInternal(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
            null,
        )
    }

    private fun mapToParamsInternal(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod?,
    ): CardComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val cardConfiguration = checkoutConfiguration.getCardConfiguration()
        return mapToParams(
            commonComponentParamsMapperData.commonComponentParams,
            commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams,
            cardConfiguration,
            paymentMethod,
        )
    }

    private fun mapToParams(
        commonComponentParams: CommonComponentParams,
        sessionParams: SessionParams?,
        dropInOverrideParams: DropInOverrideParams?,
        cardConfiguration: CardConfiguration?,
        paymentMethod: PaymentMethod?,
    ): CardComponentParams {
        return CardComponentParams(
            commonComponentParams = commonComponentParams,
            isHolderNameRequired = cardConfiguration?.isHolderNameRequired ?: false,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: cardConfiguration?.isSubmitButtonVisible ?: true,
            supportedCardBrands = getSupportedCardBrands(cardConfiguration, paymentMethod),
            shopperReference = cardConfiguration?.shopperReference,
            isStorePaymentFieldVisible = getStorePaymentFieldVisible(sessionParams, cardConfiguration),
            socialSecurityNumberVisibility = cardConfiguration?.socialSecurityNumberVisibility
                ?: SocialSecurityNumberVisibility.HIDE,
            kcpAuthVisibility = cardConfiguration?.kcpAuthVisibility ?: KCPAuthVisibility.HIDE,
            installmentParams = getInstallmentParams(
                sessionParams,
                cardConfiguration,
                commonComponentParams.amount,
                commonComponentParams.shopperLocale,
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
                CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST
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

    private fun getInstallmentParams(
        sessionParams: SessionParams?,
        cardConfiguration: CardConfiguration?,
        amount: Amount?,
        shopperLocale: Locale
    ): InstallmentParams? {
        return if (sessionParams != null) {
            // we don't fall back to the original value of installmentParams value on purpose
            // if sessionParams.installmentOptions is null we want installmentParams to be also null regardless of what
            // InstallmentConfiguration is passed to the mapper
            installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = sessionParams.installmentConfiguration,
                amount = amount,
                shopperLocale = shopperLocale,
            )
        } else {
            installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = cardConfiguration?.installmentConfiguration,
                amount = amount,
                shopperLocale = shopperLocale,
            )
        }
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
}
