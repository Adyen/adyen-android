/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.internal.CheckoutParams

internal class CardConfigDataGenerator(
    private val checkoutParams: CheckoutParams,
) {

    fun generate(params: CardComponentParams, isStored: Boolean): Map<String, String> {
        return buildMap {
            put("billingAddressMode", params.getBillingAddressMode())
            put("billingAddressRequired", params.getBillingAddressRequired())
            put("brands", params.supportedCardBrands.joinToString(",") { it.txVariant })
            put("showSupportedCardBrandLogos", params.showSupportedCardBrandLogos.toString())
            put("enableStoreDetails", params.showStorePaymentMethod.toString())
            put("hasHolderName", params.showCardholderName.toString())
            put("hasInstallmentOptions", (params.installmentParams != null).toString())
            put("hideCVC", params.getHideCVC(isStored))
            put("holderNameRequired", params.showCardholderName.toString())

            if (params.installmentParams != null) {
                put("showInstallmentAmounts", params.installmentParams.showInstallmentAmount.toString())
            }

            put("showKCPType", params.koreanAuthenticationVisibility.toTrackableValue())
            put("showPayButton", checkoutParams.showSubmitButton.toString())
            put("socialSecurityNumberMode", params.socialSecurityNumberVisibility.toTrackableValue())
            put("showCardScanner", params.showCardScanner.toString())
        }
    }

    // TODO - Adjust this method when we support full address
    private fun CardComponentParams.getBillingAddressMode(): String {
        return if (showPostalCode) "PostalCode" else "None"
    }

    // TODO - Adjust this method when we support full address
    private fun CardComponentParams.getBillingAddressRequired(): String {
        return if (showPostalCode) "true" else "false"
    }

    private fun CardComponentParams.getHideCVC(isStored: Boolean): String {
        return if (isStored) {
            when (storedCVCVisibility) {
                StoredCVCVisibility.SHOW -> "show"
                StoredCVCVisibility.HIDE -> "hide"
            }
        } else {
            when (cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW -> "show"
                CVCVisibility.ALWAYS_HIDE -> "hide"
                CVCVisibility.HIDE_FIRST -> "auto"
            }
        }
    }

    private fun FieldVisibility.toTrackableValue(): String {
        return when (this) {
            FieldVisibility.HIDE -> "hide"
            FieldVisibility.SHOW -> "show"
        }
    }
}
