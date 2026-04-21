/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/4/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.helper.toCardBrandData
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal class UpdateDetectedCardTypesIntentHandler(
    private val componentParams: CardComponentParams,
) {
    fun updateCardComponentState(
        state: CardComponentState,
        intent: CardIntent.UpdateDetectedCardTypes,
    ): CardComponentState {
        val detectedCardTypes = intent.detectedCardTypeList.detectedCardTypes
        val isDetectedFromNetwork = intent.detectedCardTypeList.source == DetectedCardTypeList.Source.NETWORK

        val supportedDetectedCardTypes = detectedCardTypes.filter { it.isSupported }

        val cardBrandState = when {
            // local detection + no supported brands
            !isDetectedFromNetwork && supportedDetectedCardTypes.isEmpty() -> {
                CardBrandState.NoBrandsDetected
            }

            // local detection + supported brands
            !isDetectedFromNetwork -> {
                // select the first brand and discard the rest
                CardBrandState.SingleBrand(supportedDetectedCardTypes.first().toCardBrandData(), false)
            }

            // network detection + no detected brands
            detectedCardTypes.isEmpty() -> {
                // Bin lookup did not return any brands
                CardBrandState.NoBrandsDetected
            }

            // network detection + detected brands but no supported brands
            supportedDetectedCardTypes.isEmpty() -> {
                CardBrandState.UnsupportedBrand
            }

            // network detection + 1 detected brand
            supportedDetectedCardTypes.size == 1 -> {
                CardBrandState.SingleBrand(supportedDetectedCardTypes.first().toCardBrandData(), true)
            }

            // network detection + multiple detected brands
            else -> {
                // select the first 2 brands and discard the rest (should only have 2 brands normally)
                CardBrandState.DualBrand(supportedDetectedCardTypes.take(2).map { it.toCardBrandData() })
            }
        }

        val selectedOrFirstReliableCardType = when {
            state.selectedCardBrand != null -> {
                supportedDetectedCardTypes.firstOrNull { it.cardBrand.txVariant == state.selectedCardBrand.txVariant }
            }

            isDetectedFromNetwork -> supportedDetectedCardTypes.firstOrNull()
            else -> null
        }
        val cvcPolicy = selectedOrFirstReliableCardType?.cvcPolicy
        val expiryDatePolicy = selectedOrFirstReliableCardType?.expiryDatePolicy

        return state.copy(
            cardBrandState = cardBrandState,
            securityCode = state.securityCode.copy(
                requirementPolicy = getSecurityCodeRequirementPolicy(cvcPolicy),
            ),
            expiryDate = state.expiryDate.copy(
                requirementPolicy = getExpiryDateRequirementPolicy(expiryDatePolicy),
            ),
        )
    }

    private fun getExpiryDateRequirementPolicy(expiryDatePolicy: Brand.FieldPolicy?): RequirementPolicy {
        if (expiryDatePolicy == null) return RequirementPolicy.Required

        return when (expiryDatePolicy) {
            Brand.FieldPolicy.REQUIRED -> RequirementPolicy.Required
            Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
            Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
        }
    }

    private fun getSecurityCodeRequirementPolicy(cvcPolicy: Brand.FieldPolicy?): RequirementPolicy {
        return if (cvcPolicy != null) {
            when (componentParams.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW, CVCVisibility.HIDE_FIRST -> {
                    when (cvcPolicy) {
                        Brand.FieldPolicy.REQUIRED -> RequirementPolicy.Required
                        Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
                        Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
                    }
                }

                CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
            }
        } else {
            when (componentParams.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
                CVCVisibility.HIDE_FIRST, CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
            }
        }
    }
}
