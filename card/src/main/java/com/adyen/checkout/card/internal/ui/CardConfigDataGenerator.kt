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
        return buildMap {
            if (configuration.addressParams is AddressParams.FullAddress) {
                val countryList = configuration.addressParams.supportedCountryCodes.joinToString(",")
                put("billingAddressAllowedCountries", countryList)
            }

            put("billingAddressMode", getBillingAddressMode(configuration.addressParams))
            put("billingAddressRequired", (configuration.addressParams !is AddressParams.None).toString())
            put("brands", configuration.supportedCardBrands.joinToString(",") { it.txVariant })
            put("enableStoreDetails", configuration.isStorePaymentFieldVisible.toString())
            put("hasHolderName", configuration.isHolderNameRequired.toString())
            put("hasInstallmentOptions", (configuration.installmentParams != null).toString())
            put("hideCVC", getHideCVC(configuration, isStored))
            put("holderNameRequired", configuration.isHolderNameRequired.toString())

            if (configuration.installmentParams != null) {
                put("showInstallmentAmounts", configuration.installmentParams.showInstallmentAmount.toString())
            }

            put("showKCPType", getShowKCPType(configuration.kcpAuthVisibility))
            put("showPayButton", configuration.isSubmitButtonVisible.toString())
            put("socialSecurityNumberMode", getSocialSecurityNumberMode(configuration.socialSecurityNumberVisibility))
        }
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
