/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.BillingAddressMode
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.internal.AdditionalSessionParams
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import java.util.Locale

// TODO - Card Component Mapper Tests.
internal class CardComponentParamsMapper(
    private val installmentsParamsMapper: InstallmentsParamsMapper = InstallmentsParamsMapper(),
) {

    fun mapToParams(
        params: CheckoutParams,
        paymentMethod: CardPaymentMethod?,
    ): CardComponentParams {
        val cardConfiguration = params.getConfiguration<CardConfiguration>()
        return CardComponentParams(
            showCardholderName = cardConfiguration?.showCardholderName ?: false,
            supportedCardBrands = getSupportedCardBrands(cardConfiguration, paymentMethod),
            showStorePaymentMethod = getStorePaymentFieldVisible(params.additionalSessionParams, cardConfiguration),
            showSupportedCardBrandLogos = cardConfiguration?.showSupportedCardBrandLogos ?: true,
            socialSecurityNumberVisibility = cardConfiguration?.socialSecurityNumberVisibility
                ?: FieldVisibility.HIDE,
            koreanAuthenticationVisibility = cardConfiguration?.koreanAuthenticationVisibility
                ?: FieldVisibility.HIDE,
            showPostalCode = cardConfiguration?.billingAddressMode is BillingAddressMode.PostalCode,
            cvcVisibility = if (cardConfiguration?.showSecurityCode == false) {
                CVCVisibility.ALWAYS_HIDE
            } else {
                CVCVisibility.ALWAYS_SHOW
            },
            storedCVCVisibility = if (cardConfiguration?.showSecurityCodeForStoredCard == false) {
                StoredCVCVisibility.HIDE
            } else {
                StoredCVCVisibility.SHOW
            },
            showCardScanner = cardConfiguration?.showCardScanner ?: true,
            installmentParams = getInstallmentParams(
                sessionParams,
                cardConfiguration,
                commonComponentParams.amount,
            ),
        )
    }

    /**
     * Check which set of supported cards to pass to the component.
     * Priority is: Custom -> PaymentMethod.brands -> Default
     */
    private fun getSupportedCardBrands(
        cardConfiguration: CardConfiguration?,
        paymentMethod: CardPaymentMethod?
    ): List<CardBrand> {
        val supportedCardBrands = cardConfiguration?.supportedCardBrands
        return when {
            !supportedCardBrands.isNullOrEmpty() -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Reading supportedCardTypes from configuration" }
                supportedCardBrands
            }

            paymentMethod?.brands?.isNotEmpty() == true -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Reading supportedCardTypes from API brands" }
                paymentMethod.brands.map {
                    CardBrand(txVariant = it)
                }
            }

            else -> {
                adyenLog(AdyenLogLevel.VERBOSE) { "Falling back to CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST" }
                DEFAULT_SUPPORTED_CARDS_LIST
            }
        }
    }

    private fun getStorePaymentFieldVisible(
        sessionParams: AdditionalSessionParams?,
        cardConfiguration: CardConfiguration?,
    ): Boolean {
        return sessionParams?.enableStoreDetails ?: cardConfiguration?.showStorePaymentMethod ?: true
    }

    private fun getInstallmentParams(
        sessionParams: SessionParams?,
        cardConfiguration: CardConfiguration?,
        amount: Amount?,
    ): InstallmentParams? {
        return if (sessionParams != null) {
            installmentsParamsMapper.mapToInstallmentParams(
                sessionInstallmentConfiguration = sessionParams.installmentConfiguration,
                amount = amount,
            )
        } else {
            installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = cardConfiguration?.installmentConfiguration,
                amount = amount,
            )
        }
    }

    companion object {
        val DEFAULT_SUPPORTED_CARDS_LIST: List<CardBrand> = listOf(
            CardBrand(CardType.VISA.txVariant),
            CardBrand(CardType.AMERICAN_EXPRESS.txVariant),
            CardBrand(CardType.MASTERCARD.txVariant),
        )
    }
}
