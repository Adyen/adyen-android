/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class StoredCardComponentStateFactoryTest {

    @Nested
    inner class SecurityCodeTest {

        @Test
        fun `when storedCVCVisibility is SHOW, then securityCode requirementPolicy is Required`() {
            val state = createFactory(storedCVCVisibility = StoredCVCVisibility.SHOW).createInitialState()

            assertEquals(RequirementPolicy.Required, state.securityCode.requirementPolicy)
        }

        @Test
        fun `when storedCVCVisibility is HIDE, then securityCode requirementPolicy is Hidden`() {
            val state = createFactory(storedCVCVisibility = StoredCVCVisibility.HIDE).createInitialState()

            assertEquals(RequirementPolicy.Hidden, state.securityCode.requirementPolicy)
        }

        @Test
        fun `when initial state is created, then securityCode is focused`() {
            val state = createFactory().createInitialState()

            assertTrue(state.securityCode.isFocused)
        }
    }

    @Nested
    inner class IsLoadingTest {

        @Test
        fun `when initial state is created, then isLoading is false`() {
            val state = createFactory().createInitialState()

            assertFalse(state.isLoading)
        }
    }

    @Nested
    inner class DetectedCardTypeTest {

        @Test
        fun `when initial state is created, then detectedCardType cardBrand matches stored brand`() {
            val brand = CardType.VISA.txVariant
            val state = createFactory(brand = brand).createInitialState()

            assertEquals(CardBrand(txVariant = brand), state.detectedCardType?.cardBrand)
        }

        @Test
        fun `when initial state is created, then detectedCardType enableLuhnCheck is true`() {
            val state = createFactory().createInitialState()

            assertTrue(state.detectedCardType!!.enableLuhnCheck)
        }

        @Test
        fun `when initial state is created, then detectedCardType expiryDatePolicy is REQUIRED`() {
            val state = createFactory().createInitialState()

            assertEquals(Brand.FieldPolicy.REQUIRED, state.detectedCardType?.expiryDatePolicy)
        }

        @Test
        fun `when initial state is created, then detectedCardType isSupported is true`() {
            val state = createFactory().createInitialState()

            assertTrue(state.detectedCardType!!.isSupported)
        }

        @Test
        fun `when initial state is created, then detectedCardType isHidden is false`() {
            val state = createFactory().createInitialState()

            assertFalse(state.detectedCardType!!.isHidden)
        }

        @Test
        fun `when initial state is created, then detectedCardType isShopperSelectionAllowedInDualBranded is false`() {
            val state = createFactory().createInitialState()

            assertFalse(state.detectedCardType!!.isShopperSelectionAllowedInDualBranded)
        }

        @Test
        fun `when initial state is created, then detectedCardType panLength is null`() {
            val state = createFactory().createInitialState()

            assertNull(state.detectedCardType?.panLength)
        }

        @Test
        fun `when initial state is created, then detectedCardType paymentMethodVariant is null`() {
            val state = createFactory().createInitialState()

            assertNull(state.detectedCardType?.paymentMethodVariant)
        }

        @Test
        fun `when initial state is created, then detectedCardType localizedBrand is null`() {
            val state = createFactory().createInitialState()

            assertNull(state.detectedCardType?.localizedBrand)
        }
    }

    @Nested
    inner class CvcPolicyTest {

        @Test
        fun `when storedCVCVisibility is SHOW, then cvcPolicy is REQUIRED`() {
            val state = createFactory(storedCVCVisibility = StoredCVCVisibility.SHOW).createInitialState()

            assertEquals(Brand.FieldPolicy.REQUIRED, state.detectedCardType?.cvcPolicy)
        }

        @Test
        fun `when storedCVCVisibility is HIDE, then cvcPolicy is HIDDEN`() {
            val state = createFactory(storedCVCVisibility = StoredCVCVisibility.HIDE).createInitialState()

            assertEquals(Brand.FieldPolicy.HIDDEN, state.detectedCardType?.cvcPolicy)
        }

        @Test
        fun `when brand is BCMC and storedCVCVisibility is SHOW, then cvcPolicy is HIDDEN`() {
            val state = createFactory(
                brand = CardType.BCMC.txVariant,
                storedCVCVisibility = StoredCVCVisibility.SHOW,
            ).createInitialState()

            assertEquals(Brand.FieldPolicy.HIDDEN, state.detectedCardType?.cvcPolicy)
        }

        @Test
        fun `when brand is BCMC and storedCVCVisibility is HIDE, then cvcPolicy is HIDDEN`() {
            val state = createFactory(
                brand = CardType.BCMC.txVariant,
                storedCVCVisibility = StoredCVCVisibility.HIDE,
            ).createInitialState()

            assertEquals(Brand.FieldPolicy.HIDDEN, state.detectedCardType?.cvcPolicy)
        }

        @Test
        fun `when brand is not BCMC and storedCVCVisibility is SHOW, then cvcPolicy is REQUIRED`() {
            val state = createFactory(
                brand = CardType.MASTERCARD.txVariant,
                storedCVCVisibility = StoredCVCVisibility.SHOW,
            ).createInitialState()

            assertEquals(Brand.FieldPolicy.REQUIRED, state.detectedCardType?.cvcPolicy)
        }
    }

    private fun createFactory(
        brand: String = CardType.VISA.txVariant,
        storedCVCVisibility: StoredCVCVisibility = StoredCVCVisibility.SHOW,
    ) = StoredCardComponentStateFactory(
        storedPaymentMethod = StoredCardPaymentMethod(
            type = "scheme",
            name = "Test Card",
            id = "test_id",
            supportedShopperInteractions = listOf("Ecommerce"),
            brand = brand,
            lastFour = "1234",
            expiryMonth = "03",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        ),
        componentParams = CardComponentParams(
            showCardholderName = false,
            supportedCardBrands = emptyList(),
            showStorePaymentMethod = false,
            showSupportedCardBrandLogos = true,
            socialSecurityNumberVisibility = FieldVisibility.HIDE,
            koreanAuthenticationVisibility = FieldVisibility.HIDE,
            showPostalCode = false,
            cvcVisibility = CVCVisibility.ALWAYS_SHOW,
            storedCVCVisibility = storedCVCVisibility,
            showCardScanner = true,
            installmentParams = null,
        ),
    )
}
