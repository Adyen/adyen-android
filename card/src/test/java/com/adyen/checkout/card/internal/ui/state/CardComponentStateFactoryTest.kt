/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/5/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentOptionsParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CardComponentStateFactoryTest {

    // region cardNumber
    @Test
    fun `when initial state is created, then card number is focused`() {
        val state = createFactory().createInitialState()

        assertTrue(state.cardNumber.isFocused)
    }
    // endregion

    // region securityCode
    @Test
    fun `when cvcVisibility is ALWAYS_SHOW, then securityCode is Required`() {
        val state = createFactory(cvcVisibility = CVCVisibility.ALWAYS_SHOW).createInitialState()

        assertEquals(RequirementPolicy.Required, state.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is HIDE_FIRST, then securityCode is Hidden`() {
        val state = createFactory(cvcVisibility = CVCVisibility.HIDE_FIRST).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_HIDE, then securityCode is Hidden`() {
        val state = createFactory(cvcVisibility = CVCVisibility.ALWAYS_HIDE).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.securityCode.requirementPolicy)
    }
    // endregion

    // region holderName
    @Test
    fun `when showCardholderName is true, then holderName is Required`() {
        val state = createFactory(showCardholderName = true).createInitialState()

        assertEquals(RequirementPolicy.Required, state.holderName.requirementPolicy)
    }

    @Test
    fun `when showCardholderName is false, then holderName is Hidden`() {
        val state = createFactory(showCardholderName = false).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.holderName.requirementPolicy)
    }
    // endregion

    // region socialSecurityNumber
    @Test
    fun `when socialSecurityNumberVisibility is SHOW, then socialSecurityNumber is Required`() {
        val state = createFactory(socialSecurityNumberVisibility = FieldVisibility.SHOW).createInitialState()

        assertEquals(RequirementPolicy.Required, state.socialSecurityNumber.requirementPolicy)
    }

    @Test
    fun `when socialSecurityNumberVisibility is HIDE, then socialSecurityNumber is Hidden`() {
        val state = createFactory(socialSecurityNumberVisibility = FieldVisibility.HIDE).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.socialSecurityNumber.requirementPolicy)
    }
    // endregion

    // region koreanAuthentication
    @Test
    fun `when koreanAuthenticationVisibility is SHOW, then kcp fields are Required`() {
        val state = createFactory(koreanAuthenticationVisibility = FieldVisibility.SHOW).createInitialState()

        assertEquals(RequirementPolicy.Required, state.kcpBirthDateOrTaxNumber.requirementPolicy)
        assertEquals(RequirementPolicy.Required, state.kcpCardPassword.requirementPolicy)
    }

    @Test
    fun `when koreanAuthenticationVisibility is HIDE, then kcp fields are Hidden`() {
        val state = createFactory(koreanAuthenticationVisibility = FieldVisibility.HIDE).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.kcpBirthDateOrTaxNumber.requirementPolicy)
        assertEquals(RequirementPolicy.Hidden, state.kcpCardPassword.requirementPolicy)
    }
    // endregion

    // region postalCode
    @Test
    fun `when showPostalCode is true, then postalCode is Required`() {
        val state = createFactory(showPostalCode = true).createInitialState()

        assertEquals(RequirementPolicy.Required, state.postalCode.requirementPolicy)
    }

    @Test
    fun `when showPostalCode is false, then postalCode is Hidden`() {
        val state = createFactory(showPostalCode = false).createInitialState()

        assertEquals(RequirementPolicy.Hidden, state.postalCode.requirementPolicy)
    }
    // endregion

    // region storePayment
    @Test
    fun `when showStorePaymentMethod is true, then isStorePaymentFieldVisible is true`() {
        val state = createFactory(showStorePaymentMethod = true).createInitialState()

        assertTrue(state.isStorePaymentFieldVisible)
    }

    @Test
    fun `when showStorePaymentMethod is false, then isStorePaymentFieldVisible is false`() {
        val state = createFactory(showStorePaymentMethod = false).createInitialState()

        assertFalse(state.isStorePaymentFieldVisible)
    }

    @Test
    fun `when initial state is created, then storePaymentMethod is false`() {
        val state = createFactory().createInitialState()

        assertFalse(state.storePaymentMethod)
    }
    // endregion

    // region supportedCardBrands
    @Test
    fun `when supportedCardBrands is set, then state contains those brands`() {
        val brands = listOf(CardBrand(CardType.VISA.txVariant), CardBrand(CardType.MASTERCARD.txVariant))
        val state = createFactory(supportedCardBrands = brands).createInitialState()

        assertEquals(brands, state.supportedCardBrands)
    }
    // endregion

    // region showSupportedCardBrandLogos
    @Test
    fun `when showSupportedCardBrandLogos is true, then state has showSupportedCardBrandLogos true`() {
        val state = createFactory(showSupportedCardBrandLogos = true).createInitialState()

        assertTrue(state.showSupportedCardBrandLogos)
    }

    @Test
    fun `when showSupportedCardBrandLogos is false, then state has showSupportedCardBrandLogos false`() {
        val state = createFactory(showSupportedCardBrandLogos = false).createInitialState()

        assertFalse(state.showSupportedCardBrandLogos)
    }
    // endregion

    // region defaults
    @Test
    fun `when initial state is created, then isLoading is false`() {
        val state = createFactory().createInitialState()

        assertFalse(state.isLoading)
    }

    @Test
    fun `when initial state is created, then cardBrandState is NoBrandsDetected`() {
        val state = createFactory().createInitialState()

        assertEquals(CardBrandState.NoBrandsDetected, state.cardBrandState)
    }
    // endregion

    // region installments
    @Test
    fun `when initial state is created and installmentParams is provided, then installmentState is populated`() {
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(2, 3),
                plans = listOf(InstallmentPlan.REGULAR)
            ),
            showInstallmentAmount = true
        )
        val state = createFactory(installmentParams = installmentParams).createInitialState()

        assertEquals(3, state.installmentState.installmentOptions.size) // OneTime, 2, 3
        assertEquals(null, state.installmentState.selectedInstallment)
    }

    @Test
    fun `when initial state is created and installmentParams has preselectedValue, then selectedInstallment is set`() {
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(2, 3),
                plans = listOf(InstallmentPlan.REGULAR),
                preselectedValue = 3
            ),
            showInstallmentAmount = true
        )
        val state = createFactory(installmentParams = installmentParams).createInitialState()

        assertEquals(3, state.installmentState.installmentOptions.size)
        assertEquals(InstallmentPlan.REGULAR, state.installmentState.selectedInstallment?.plan)
        assertEquals(3, state.installmentState.selectedInstallment?.numberOfInstallments)
    }
    // endregion

    @Suppress("LongParameterList")
    private fun createFactory(
        showCardholderName: Boolean = false,
        supportedCardBrands: List<CardBrand> = emptyList(),
        showStorePaymentMethod: Boolean = false,
        showSupportedCardBrandLogos: Boolean = true,
        socialSecurityNumberVisibility: FieldVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility: FieldVisibility = FieldVisibility.HIDE,
        showPostalCode: Boolean = false,
        cvcVisibility: CVCVisibility = CVCVisibility.ALWAYS_SHOW,
        installmentParams: InstallmentParams = InstallmentParams(),
    ) = CardComponentStateFactory(
        componentParams = CardComponentParams(
            showCardholderName = showCardholderName,
            supportedCardBrands = supportedCardBrands,
            showStorePaymentMethod = showStorePaymentMethod,
            showSupportedCardBrandLogos = showSupportedCardBrandLogos,
            socialSecurityNumberVisibility = socialSecurityNumberVisibility,
            koreanAuthenticationVisibility = koreanAuthenticationVisibility,
            showPostalCode = showPostalCode,
            cvcVisibility = cvcVisibility,
            storedCVCVisibility = StoredCVCVisibility.SHOW,
            showCardScanner = true,
            installmentParams = installmentParams,
        ),
    )
}
