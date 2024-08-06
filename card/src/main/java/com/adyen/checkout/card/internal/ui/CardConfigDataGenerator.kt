/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/8/2024.
 */

package com.adyen.checkout.card.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardConfigDataGenerator {

    fun generate(
        configuration: CardComponentParams,
        isStored: Boolean,
    ): Map<String, String> {
        return mapOf(
            // TODO Check if we need to send null or empty list
            "billingAddressAllowedCountries" to ((configuration.addressParams as? AddressParams.FullAddress)
                ?.supportedCountryCodes?.joinToString(",") ?: ""),
            "billingAddressMode" to getBillingAddressMode(configuration.addressParams),
            "billingAddressRequired" to (configuration.addressParams !is AddressParams.None).toString(),
            "brands" to configuration.supportedCardBrands.joinToString(",") { it.txVariant },
            "enableStoreDetails" to configuration.isStorePaymentFieldVisible.toString(),
            "hasHolderName" to configuration.isHolderNameRequired.toString(),
            "hasInstallmentOptions" to (configuration.installmentParams != null).toString(),
            "hideCVC" to getHideCVC(configuration, isStored),
            "holderNameRequired" to configuration.isHolderNameRequired.toString(),
            "showInstallmentAmounts" to (configuration.installmentParams?.showInstallmentAmount ?: false).toString(),
            "showKCPType" to getShowKCPType(configuration.kcpAuthVisibility),
            "showPayButton" to configuration.isSubmitButtonVisible.toString(),
            "socialSecurityNumberMode" to getSocialSecurityNumberMode(configuration.socialSecurityNumberVisibility),
        )
    }

    private fun getBillingAddressMode(addressParams: AddressParams): String {
        return when (addressParams) {
            is AddressParams.FullAddress -> "full"
            is AddressParams.Lookup -> "lookup"
            AddressParams.None -> "none"
            is AddressParams.PostalCode -> "partial"
        }
    }

    private fun getHideCVC(configuration: CardComponentParams, isStored: Boolean): String {
        return if (isStored) {
            when (configuration.storedCVCVisibility) {
                StoredCVCVisibility.SHOW -> "show"
                StoredCVCVisibility.HIDE -> "hide"
            }
        } else {
            when (configuration.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW -> "show"
                CVCVisibility.ALWAYS_HIDE -> "hide"
                CVCVisibility.HIDE_FIRST -> "auto"
            }
        }
    }

    private fun getShowKCPType(kcpAuthVisibility: KCPAuthVisibility): String {
        return when (kcpAuthVisibility) {
            KCPAuthVisibility.SHOW -> "show"
            KCPAuthVisibility.HIDE -> "hide"
        }
    }

    private fun getSocialSecurityNumberMode(socialSecurityNumberVisibility: SocialSecurityNumberVisibility): String {
        return when (socialSecurityNumberVisibility) {
            SocialSecurityNumberVisibility.SHOW -> "show"
            SocialSecurityNumberVisibility.HIDE -> "hide"
        }
    }
}
