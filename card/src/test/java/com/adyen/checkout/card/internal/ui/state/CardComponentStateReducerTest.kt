/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class CardComponentStateReducerTest(
    @Mock private val cardComponentParams: CardComponentParams,
) {

    private lateinit var reducer: CardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = CardComponentStateReducer(cardComponentParams)
    }

    @Test
    fun `when intent is UpdateCardNumber, then cardNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardNumber("4111111111111111"))

        assertEquals("4111111111111111", actual.cardNumber.text)
    }

    @Test
    fun `when intent is UpdateCardNumberFocus, then cardNumber focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardNumberFocus(true))

        assertTrue(actual.cardNumber.isFocused)
    }

    @Test
    fun `when intent is UpdateExpiryDate, then expiryDate state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDate("1225"))

        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateExpiryDateFocus, then expiryDate focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDateFocus(true))

        assertTrue(actual.expiryDate.isFocused)
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateSecurityCodeFocus, then securityCode focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCodeFocus(true))

        assertTrue(actual.securityCode.isFocused)
    }

    @Test
    fun `when intent is UpdateHolderName, then holderName state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderName("John Doe"))

        assertEquals("John Doe", actual.holderName.text)
    }

    @Test
    fun `when intent is UpdateHolderNameFocus, then holderName focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderNameFocus(true))

        assertTrue(actual.holderName.isFocused)
    }

    @Test
    fun `when intent is UpdateStorePaymentMethod, then storePaymentMethod is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateStorePaymentMethod(true))

        assertTrue(actual.storePaymentMethod)
    }

    @Test
    fun `when intent is SelectBrand, then selectedCardBrand is updated`() {
        val state = createInitialState()
        val cardBrand = CardBrand("visa")

        val actual = reducer.reduce(state, CardIntent.SelectBrand(cardBrand))

        assertEquals(cardBrand, actual.selectedCardBrand)
    }

    @Nested
    @DisplayName("when intent is UpdateDetectedCardTypes")
    inner class UpdateDetectedCardTypesTest {

        @Test
        fun `when intent is UpdateDetectedCardTypes, then detectedCardTypes state is updated with new list`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cardBrand = CardBrand("visa")),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(detectedCardTypes, actual.detectedCardTypes)
        }

        @Test
        fun `when selectedCardBrand is set, then card type matching selectedCardBrand txVariant is used for policies`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val selectedBrand = CardBrand("mc")
            val state = createInitialState().copy(selectedCardBrand = selectedBrand)
            val detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    cvcPolicy = Brand.FieldPolicy.REQUIRED,
                ),
                createDetectedCardType(
                    cardBrand = CardBrand("mc"),
                    cvcPolicy = Brand.FieldPolicy.OPTIONAL,
                ),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when selectedCardBrand is null, then first reliable and supported card type is used for policies`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isReliable = false,
                    isSupported = true,
                    cvcPolicy = Brand.FieldPolicy.REQUIRED,
                ),
                createDetectedCardType(
                    cardBrand = CardBrand("mc"),
                    isReliable = true,
                    isSupported = true,
                    cvcPolicy = Brand.FieldPolicy.OPTIONAL,
                ),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when no card type is both reliable and supported, then default policies are applied`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isReliable = false,
                    isSupported = true,
                ),
                createDetectedCardType(
                    cardBrand = CardBrand("mc"),
                    isReliable = true,
                    isSupported = false,
                ),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
            assertEquals(RequirementPolicy.Required, actual.expiryDate.requirementPolicy)
        }

        @Test
        fun `when detected card type has expiryDatePolicy REQUIRED, then expiryDate requirementPolicy is Required`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.REQUIRED),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.expiryDate.requirementPolicy)
        }

        @Test
        fun `when detected card type has expiryDatePolicy OPTIONAL, then expiryDate requirementPolicy is Optional`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.OPTIONAL),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Optional, actual.expiryDate.requirementPolicy)
        }

        @Test
        fun `when detected card type has expiryDatePolicy HIDDEN, then expiryDate requirementPolicy is Hidden`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.HIDDEN),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.expiryDate.requirementPolicy)
        }

        @Test
        fun `when detected card types list is empty, then expiryDate requirementPolicy defaults to Required`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = emptyList<DetectedCardType>()

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.expiryDate.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy REQUIRED, then securityCode requirementPolicy is Required`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy OPTIONAL, then securityCode requirementPolicy is Optional`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy HIDDEN, then securityCode requirementPolicy is Hidden`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy REQUIRED, then securityCode requirementPolicy is Required`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy OPTIONAL, then securityCode requirementPolicy is Optional`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy HIDDEN, then securityCode requirementPolicy is Hidden`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when cvcVisibility is ALWAYS_HIDE, then securityCode requirementPolicy is Hidden regardless of detected card type cvcPolicy`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_HIDE)
            val state = createInitialState()
            val detectedCardTypes = listOf(
                createDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
            )

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when detected card types list is empty and cvcVisibility is ALWAYS_SHOW, then securityCode requirementPolicy defaults to Required`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
            val state = createInitialState()
            val detectedCardTypes = emptyList<DetectedCardType>()

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when detected card types list is empty and cvcVisibility is HIDE_FIRST, then securityCode requirementPolicy defaults to Hidden`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
            val state = createInitialState()
            val detectedCardTypes = emptyList<DetectedCardType>()

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
        }

        @Test
        fun `when detected card types list is empty and cvcVisibility is ALWAYS_HIDE, then securityCode requirementPolicy defaults to Hidden`() {
            whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_HIDE)
            val state = createInitialState()
            val detectedCardTypes = emptyList<DetectedCardType>()

            val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

            assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
        }

        private fun createDetectedCardType(
            cardBrand: CardBrand = CardBrand("visa"),
            isReliable: Boolean = true,
            isSupported: Boolean = true,
            cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        ) = DetectedCardType(
            cardBrand = cardBrand,
            isReliable = isReliable,
            enableLuhnCheck = true,
            cvcPolicy = cvcPolicy,
            expiryDatePolicy = expiryDatePolicy,
            isSupported = isSupported,
            panLength = 16,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }

    @Test
    fun `when intent is UpdateLoading, then isLoading is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is HighlightValidationErrors and cardNumber has error, then cardNumber showError and focus are set`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(
                text = "",
                isFocused = false,
                errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE,
                showError = false,
            ),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertTrue(actual.cardNumber.showError)
        assertTrue(actual.cardNumber.isFocused)
    }

    @Test
    fun `when intent is HighlightValidationErrors and no errors, then no fields are highlighted`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.showError)
        assertFalse(actual.expiryDate.showError)
        assertFalse(actual.securityCode.showError)
        assertFalse(actual.holderName.showError)
    }

    @Test
    fun `when intent is HighlightValidationErrors with multiple errors, then first field with error gets focus`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(errorMessage = null),
            expiryDate = TextInputComponentState(errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE),
            securityCode = TextInputComponentState(errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.isFocused)
        assertTrue(actual.expiryDate.isFocused)
        assertFalse(actual.securityCode.isFocused)
    }

    private fun createInitialState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        detectedCardTypes = emptyList(),
        selectedCardBrand = null,
    )
}
