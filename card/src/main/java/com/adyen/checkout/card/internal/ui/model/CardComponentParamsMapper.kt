/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.BillingAddressMode
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.AdditionalSessionParams
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod

// TODO - Card Component Mapper Tests.
internal class CardComponentParamsMapper {

    fun mapToParams(
        params: CheckoutParams,
        paymentMethod: CardPaymentMethod?,
    ): CardComponentParams {
        val cardConfiguration = params.getConfiguration<CardConfiguration>()
        return CardComponentParams(
            showCardholderName = cardConfiguration?.showCardholderName ?: false,
            supportedCardBrands = getSupportedCardBrands(cardConfiguration, paymentMethod),
            storePaymentMethodMode = getStorePaymentMethodMode(params),
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
            installmentParams = makeInstallmentParams(
                sessionParams = params.additionalSessionParams,
                cardConfiguration = cardConfiguration,
                amount = params.amount,
                paymentMethod = paymentMethod,
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

    private fun getStorePaymentMethodMode(params: CheckoutParams): CardStorePaymentMethodMode {
        return when {
            params.amount?.value == 0L -> CardStorePaymentMethodMode.ALWAYS
            params.isStorePaymentFieldEnabled() -> CardStorePaymentMethodMode.ASK_FOR_CONSENT
            else -> CardStorePaymentMethodMode.HIDDEN
        }
    }

    private fun makeInstallmentParams(
        sessionParams: AdditionalSessionParams?,
        cardConfiguration: CardConfiguration?,
        amount: Amount?,
        paymentMethod: CardPaymentMethod?,
    ): InstallmentParams? {
        return when {
            paymentMethod?.fundingSource.equals(DEBIT_FUNDING_SOURCE, ignoreCase = true) -> null
            // if sessionParams.installmentOptions is null we want installmentParams to be also null
            sessionParams != null -> sessionParams.installmentConfiguration?.mapToInstallmentParams(amount)
            else -> cardConfiguration?.installmentConfiguration?.mapToInstallmentParams(amount)
        }
    }

    companion object {
        @VisibleForTesting
        internal val DEFAULT_SUPPORTED_CARDS_LIST: List<CardBrand> = listOf(
            CardBrand.VISA,
            CardBrand.AMERICAN_EXPRESS,
            CardBrand.MASTERCARD,
        )

        private const val DEBIT_FUNDING_SOURCE = "debit"
    }
}

/**
 * Whether the store payment method field is enabled by the session or the merchant configuration, before the amount
 * is taken into account.
 */
internal fun CheckoutParams.isStorePaymentFieldEnabled(): Boolean {
    return additionalSessionParams?.enableStoreDetails
        ?: getConfiguration<CardConfiguration>()?.showStorePaymentMethod
        ?: true
}
