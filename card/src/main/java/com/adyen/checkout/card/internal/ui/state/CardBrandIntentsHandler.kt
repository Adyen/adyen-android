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

internal class CardBrandIntentsHandler(
    private val componentParams: CardComponentParams,
) {
    fun onUpdateDetectedCardTypes(
        state: CardComponentState,
        intent: CardIntent.UpdateDetectedCardTypes,
    ): CardComponentState {
        val cardBrandState = getCardBrandState(state, intent)

        val selectedOrFirstOrReliableCardBrandData = when (cardBrandState) {
            is CardBrandState.DualBrand -> {
                cardBrandState.shopperSelectedCardBrandData ?: cardBrandState.cardBrandDataList.firstOrNull()
            }

            is CardBrandState.SingleBrand if cardBrandState.isReliable -> cardBrandState.cardBrandData
            else -> null
        }
        val cvcPolicy = selectedOrFirstOrReliableCardBrandData?.cvcPolicy
        val expiryDatePolicy = selectedOrFirstOrReliableCardBrandData?.expiryDatePolicy

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

    private fun getCardBrandState(
        currentState: CardComponentState,
        intent: CardIntent.UpdateDetectedCardTypes,
    ): CardBrandState {
        val detectedCardTypes = intent.detectedCardTypeList.detectedCardTypes
        val isDetectedFromNetwork = intent.detectedCardTypeList.source == DetectedCardTypeList.Source.NETWORK

        val supportedDetectedCardTypes = detectedCardTypes.filter { it.isSupported }

        return when {
            // local detection + no supported brands
            !isDetectedFromNetwork && supportedDetectedCardTypes.isEmpty() -> {
                CardBrandState.NoBrandsDetected
            }

            // local detection + supported brands
            !isDetectedFromNetwork -> {
                // select the first brand and discard the rest
                CardBrandState.SingleBrand(
                    cardBrandData = supportedDetectedCardTypes.first().toCardBrandData(),
                    isReliable = false,
                )
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
                CardBrandState.SingleBrand(
                    cardBrandData = supportedDetectedCardTypes.first().toCardBrandData(),
                    isReliable = true,
                )
            }

            // network detection + multiple detected brands
            else -> {
                // select the first 2 brands and discard the rest (should only have 2 brands normally)
                val firstTwoDetectedCardTypes = supportedDetectedCardTypes.take(2)
                val cardBrandDataList = firstTwoDetectedCardTypes.map { it.toCardBrandData() }

                val shopperSelectionAllowed = firstTwoDetectedCardTypes.any {
                    it.isShopperSelectionAllowedInDualBranded
                }
                val currentCardBrandState = currentState.cardBrandState
                val shopperSelectedCardBrandData = if (
                    currentCardBrandState is CardBrandState.DualBrand &&
                    currentCardBrandState.cardBrandDataList == cardBrandDataList
                ) {
                    // the list of brands has not changed, keep the previously selected brand
                    currentCardBrandState.shopperSelectedCardBrandData
                } else if (shopperSelectionAllowed) {
                    // auto select the first brand
                    cardBrandDataList.firstOrNull()
                } else {
                    null
                }

                CardBrandState.DualBrand(
                    cardBrandDataList = cardBrandDataList,
                    shopperSelectionAllowed = shopperSelectionAllowed,
                    shopperSelectedCardBrandData = shopperSelectedCardBrandData,
                )
            }
        }
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

    fun onBrandSelected(state: CardComponentState, intent: CardIntent.SelectBrand): CardComponentState {
        val currentCardBrandState = state.cardBrandState
        return if (currentCardBrandState is CardBrandState.DualBrand) {
            val selectedCardBrandData = currentCardBrandState.cardBrandDataList.firstOrNull {
                it.cardBrand == intent.cardBrand
            }
            state.copy(
                cardBrandState = currentCardBrandState.copy(
                    shopperSelectedCardBrandData = selectedCardBrandData,
                ),
            )
        } else {
            state
        }
    }
}
