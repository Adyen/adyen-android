/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/4/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.helper.DetectCardTypeBinHelper
import com.adyen.checkout.card.internal.helper.toCardBrandData
import com.adyen.checkout.card.internal.helper.toNetworkBinLookupState
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.card.internal.ui.model.mapToInstallmentModels
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import kotlin.collections.contains

internal class CardBrandIntentsHandler(
    private val componentParams: CardComponentParams,
    private val detectCardTypeBinHelper: DetectCardTypeBinHelper,
) {
    fun onUpdateDetectedCardTypes(
        state: CardComponentState,
        intent: CardIntent.UpdateDetectedCardTypes,
    ): CardComponentState {
        val shouldDiscardIntent = shouldDiscardIntent(
            currentCardNumber = state.cardNumber.text,
            intentBin = intent.detectedCardTypeList.cardDetectionBin,
        )
        return if (shouldDiscardIntent) {
            state
        } else {
            val newCardBrandState = getCardBrandState(state, intent)
            val updatedComponentState = getUpdatedCardComponentState(state, newCardBrandState)

            val networkBinLookupState = when (intent.detectedCardTypeList.source) {
                DetectedCardTypeList.Source.NETWORK -> intent.detectedCardTypeList.toNetworkBinLookupState()
                DetectedCardTypeList.Source.LOCAL -> null
            }
            updatedComponentState.copy(networkBinLookupState = networkBinLookupState)
        }
    }

    private fun shouldDiscardIntent(currentCardNumber: String, intentBin: String?): Boolean {
        val currentBin = detectCardTypeBinHelper.getCardDetectionBin(currentCardNumber)
        // result is not relevant anymore and should be discarded
        // this can happen if network results are returned after a delay and the BIN has already changed
        return intentBin != currentBin
    }

    private fun getCardBrandState(
        currentState: CardComponentState,
        intent: CardIntent.UpdateDetectedCardTypes,
    ): CardBrandState {
        val detectedCardTypes = intent.detectedCardTypeList.detectedCardTypes
        val supportedDetectedCardTypes = detectedCardTypes.filter { it.isSupported }

        return when (intent.detectedCardTypeList.source) {
            DetectedCardTypeList.Source.LOCAL -> {
                if (supportedDetectedCardTypes.isEmpty()) {
                    // local detection + no supported brands
                    CardBrandState.NoBrandsDetected
                } else {
                    // local detection + supported brands
                    // select the first brand and discard the rest
                    CardBrandState.SingleUnreliableBrand(
                        cardBrandData = supportedDetectedCardTypes.first().toCardBrandData(),
                    )
                }
            }

            DetectedCardTypeList.Source.NETWORK -> {
                val nonHiddenSupportedBrands = supportedDetectedCardTypes.filterNot {
                    it.isHidden
                }
                val anyHiddenBrandDetected = supportedDetectedCardTypes.any {
                    it.isHidden
                }

                when {
                    // network detection + no detected brands
                    detectedCardTypes.isEmpty() -> CardBrandState.NoBrandsDetected

                    // network detection + no non-hidden supported brands
                    nonHiddenSupportedBrands.isEmpty() -> {
                        if (anyHiddenBrandDetected) {
                            CardBrandState.HiddenBrand
                        } else {
                            CardBrandState.UnsupportedBrand
                        }
                    }

                    // network detection + 1 non-hidden supported brand
                    nonHiddenSupportedBrands.size == 1 -> {
                        if (anyHiddenBrandDetected) {
                            CardBrandState.SingleReliableWithHiddenBrand(
                                cardBrandData = nonHiddenSupportedBrands.first().toCardBrandData(),
                            )
                        } else {
                            CardBrandState.SingleReliableBrand(
                                cardBrandData = nonHiddenSupportedBrands.first().toCardBrandData(),
                            )
                        }
                    }

                    // network detection + multiple non-hidden supported brands
                    else -> {
                        getDualBrandedCardBrandState(currentState.cardBrandState, nonHiddenSupportedBrands)
                    }
                }
            }
        }
    }

    private fun getDualBrandedCardBrandState(
        currentCardBrandState: CardBrandState,
        supportedDetectedCardTypes: List<DetectedCardType>
    ): CardBrandState {
        // select the first 2 brands and discard the rest (should only have 2 brands normally)
        val firstTwoDetectedCardTypes = supportedDetectedCardTypes.take(2)
        val cardBrandDataList = firstTwoDetectedCardTypes.map { it.toCardBrandData() }

        val shopperSelectionAllowed = firstTwoDetectedCardTypes.any {
            it.isShopperSelectionAllowedInDualBranded
        }

        return if (shopperSelectionAllowed) {
            val shopperSelectedCardBrandData = if (
                currentCardBrandState is CardBrandState.DualBrandWithShopperSelection &&
                currentCardBrandState.cardBrandDataList == cardBrandDataList
            ) {
                // the list of brands has not changed, keep the previously selected brand
                currentCardBrandState.shopperSelectedCardBrandData
            } else {
                // auto select the first brand
                cardBrandDataList.first()
            }
            CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = cardBrandDataList,
                shopperSelectedCardBrandData = shopperSelectedCardBrandData,
            )
        } else {
            CardBrandState.DualBrand(cardBrandDataList)
        }
    }

    fun onBrandSelected(state: CardComponentState, intent: CardIntent.SelectBrand): CardComponentState {
        val currentCardBrandState = state.cardBrandState
        if (currentCardBrandState is CardBrandState.DualBrandWithShopperSelection) {
            val selectedCardBrandData = currentCardBrandState.cardBrandDataList.firstOrNull {
                it.cardBrand == intent.cardBrand
            }
            if (selectedCardBrandData != null) {
                val cardBrandState = currentCardBrandState.copy(
                    shopperSelectedCardBrandData = selectedCardBrandData,
                )
                return getUpdatedCardComponentState(state, cardBrandState)
            }
        }
        return state
    }

    @VisibleForTesting
    internal fun getUpdatedCardComponentState(
        state: CardComponentState,
        cardBrandState: CardBrandState
    ): CardComponentState {
        // We should only override the default behavior of the CVC / expiry date if the identified brand is reliable
        val reliableCardBrandData = cardBrandState.asReliableCardBrandData()
        val cvcPolicy = reliableCardBrandData?.cvcPolicy
        val expiryDatePolicy = reliableCardBrandData?.expiryDatePolicy

        return state.copy(
            cardBrandState = cardBrandState,
            securityCode = state.securityCode.copy(
                requirementPolicy = getSecurityCodeRequirementPolicy(cvcPolicy),
            ),
            expiryDate = state.expiryDate.copy(
                requirementPolicy = getExpiryDateRequirementPolicy(expiryDatePolicy),
            ),
            installmentState = getUpdatedInstallmentState(state, reliableCardBrandData?.cardBrand),
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

    private fun getUpdatedInstallmentState(
        state: CardComponentState,
        cardBrand: CardBrand?
    ): InstallmentState {
        val updatedInstallmentOptions = getUpdatedInstallmentOptions(cardBrand)
        val selectedInstallment = if (updatedInstallmentOptions.contains(state.installmentState.selectedInstallment)) {
            state.installmentState.selectedInstallment
        } else {
            val installmentParams = componentParams.installmentParams
            val preselectedValue = installmentParams?.cardBasedOptions?.get(cardBrand)
                ?: installmentParams?.defaultOptions?.preselectedValue
            updatedInstallmentOptions.firstOrNull {
                it.plan == InstallmentPlan.REGULAR && it.numberOfInstallments == preselectedValue
            }
        } ?: updatedInstallmentOptions.firstOrNull()

        return InstallmentState(updatedInstallmentOptions, selectedInstallment)
    }

    private fun getUpdatedInstallmentOptions(cardBrand: CardBrand?): List<InstallmentModel> {
        return componentParams.installmentParams?.mapToInstallmentModels(
            cardBrand = cardBrand,
        ) ?: emptyList()
    }
}

private fun CardBrandState.asReliableCardBrandData(): CardBrandData? {
    return when (this) {
        is CardBrandState.SingleReliableBrand -> cardBrandData
        is CardBrandState.SingleReliableWithHiddenBrand -> cardBrandData
        is CardBrandState.DualBrand ->
            cardBrandDataList.first() // With DualBrand (no shopper selection) we can rely on the first brand
        is CardBrandState.DualBrandWithShopperSelection -> shopperSelectedCardBrandData
        CardBrandState.HiddenBrand,
        CardBrandState.NoBrandsDetected,
        is CardBrandState.SingleUnreliableBrand,
        CardBrandState.UnsupportedBrand -> null
    }
}
