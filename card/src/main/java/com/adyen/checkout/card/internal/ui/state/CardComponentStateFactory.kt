/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.card.internal.ui.model.mapToInstallmentModels
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class CardComponentStateFactory(
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<CardComponentState> {

    override fun createInitialState(): CardComponentState {
        val installmentOptions = componentParams.installmentParams
            ?.mapToInstallmentModels()
            ?: emptyList()

        return CardComponentState(
            cardNumber = TextInputComponentState(isFocused = true),
            expiryDate = TextInputComponentState(),
            securityCode = TextInputComponentState(
                requirementPolicy = getSecurityCodeRequirementPolicy(),
            ),
            holderName = TextInputComponentState(
                requirementPolicy = getHolderNameRequirementPolicy(),
            ),
            socialSecurityNumber = TextInputComponentState(
                requirementPolicy = getSocialSecurityNumberRequirementPolicy(),
            ),
            kcpBirthDateOrTaxNumber = TextInputComponentState(
                requirementPolicy = getKcpBirthDateOrTaxNumberRequirementPolicy(),
            ),
            kcpCardPassword = TextInputComponentState(
                requirementPolicy = getKcpCardPasswordRequirementPolicy(),
            ),
            postalCode = TextInputComponentState(
                requirementPolicy = getPostalCodeRequirementPolicy(),
            ),
            storePaymentMethod = false,
            isStorePaymentFieldVisible = componentParams.showStorePaymentMethod,
            supportedCardBrands = componentParams.supportedCardBrands,
            showSupportedCardBrandLogos = componentParams.showSupportedCardBrandLogos,
            isLoading = false,
            isCardScanningAvailable = false,
            cardBrandState = CardBrandState.NoBrandsDetected,
            networkBinLookupState = null,
            installmentState = InstallmentState(
                installmentOptions = installmentOptions,
                selectedInstallment = getPreselectedInstallment(installmentOptions),
            ),
        )
    }

    private fun getSecurityCodeRequirementPolicy(): RequirementPolicy =
        when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
            CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
            CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }

    private fun getHolderNameRequirementPolicy(): RequirementPolicy =
        when (componentParams.showCardholderName) {
            true -> RequirementPolicy.Required
            false -> RequirementPolicy.Hidden
        }

    private fun getSocialSecurityNumberRequirementPolicy(): RequirementPolicy =
        when (componentParams.socialSecurityNumberVisibility) {
            FieldVisibility.SHOW -> RequirementPolicy.Required
            FieldVisibility.HIDE -> RequirementPolicy.Hidden
        }

    private fun getKcpBirthDateOrTaxNumberRequirementPolicy(): RequirementPolicy =
        when (componentParams.koreanAuthenticationVisibility) {
            FieldVisibility.SHOW -> RequirementPolicy.Required
            FieldVisibility.HIDE -> RequirementPolicy.Hidden
        }

    private fun getKcpCardPasswordRequirementPolicy(): RequirementPolicy =
        when (componentParams.koreanAuthenticationVisibility) {
            FieldVisibility.SHOW -> RequirementPolicy.Required
            FieldVisibility.HIDE -> RequirementPolicy.Hidden
        }

    private fun getPostalCodeRequirementPolicy(): RequirementPolicy =
        when (componentParams.showPostalCode) {
            true -> RequirementPolicy.Required
            false -> RequirementPolicy.Hidden
        }

    private fun getPreselectedInstallment(installmentOptions: List<InstallmentModel>): InstallmentModel? {
        val preselectedNumberOfInstallments = componentParams.installmentParams
            ?.defaultOptions
            ?.preselectedValue
            ?: return installmentOptions.firstOrNull()

        return installmentOptions.firstOrNull {
            it.plan == InstallmentPlan.REGULAR && it.numberOfInstallments == preselectedNumberOfInstallments
        } ?: installmentOptions.firstOrNull()
    }
}
