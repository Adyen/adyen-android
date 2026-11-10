/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StoredCardComponentStateValidatorTest {

    private lateinit var validator: StoredCardComponentStateValidator

    @BeforeEach
    fun beforeEach() {
        validator = StoredCardComponentStateValidator(
            cardValidationMapper = CardValidationMapper(),
        )
    }

    @Test
    fun `when security code is valid, then isValid returns true`() {
        val state = createValidState()

        val result = validator.isValid(state)

        assertTrue(result)
    }

    @Test
    fun `when security code is too short, then isValid returns false`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "1"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.securityCode.error?.message)
    }

    @Test
    fun `when security code is empty, then isValid returns false`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = ""),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.securityCode.error?.message)
    }

    @Test
    fun `when security code is valid for Amex (4 digits), then isValid returns true`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "1234"),
            detectedCardType = createDetectedCardType(cardBrand = CardBrand("amex")),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.securityCode.error?.message)
    }

    @Test
    fun `when security code is valid for non-Amex (3 digits), then isValid returns true`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "123"),
            detectedCardType = createDetectedCardType(cardBrand = CardBrand("visa")),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.securityCode.error?.message)
    }

    @Test
    fun `when the security code is hidden and empty, then isValid returns true`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Hidden),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
    }

    @Test
    fun `when required security code is empty then the three digit empty error is returned`() {
        // GIVEN
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = ""),
        )

        // WHEN
        val validatedState = validator.validate(state)

        // THEN
        assertEquals(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_EMPTY_THREE_DIGITS,
            validatedState.securityCode.error?.message,
        )
    }

    @ParameterizedTest
    @MethodSource("securityCodeErrorSource")
    fun `when the security code is validated then the expected error key is returned`(
        text: String,
        requirementPolicy: RequirementPolicy,
        cardBrand: CardBrand?,
        expectedError: CheckoutLocalizationKey?,
    ) {
        // GIVEN
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = text, requirementPolicy = requirementPolicy),
            detectedCardType = cardBrand?.let { createDetectedCardType(cardBrand = it) },
        )

        // WHEN
        val validatedState = validator.validate(state)

        // THEN
        assertEquals(expectedError, validatedState.securityCode.error?.message)
    }

    @Test
    fun `when the brand changes while a security code error is visible then the error key follows the brand`() {
        // GIVEN - a visible three-digit error on a partially filled code for a non-Amex brand
        var state = createValidState().copy(
            securityCode = TextInputComponentState(
                text = "1",
                error = TextInputComponentState.InputError(
                    CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
                    isVisible = true,
                ),
            ),
            detectedCardType = createDetectedCardType(cardBrand = CardBrand("visa")),
        )

        // WHEN the brand changes to Amex
        state = state.copy(detectedCardType = createDetectedCardType(cardBrand = CardBrand.AMERICAN_EXPRESS))
        var validatedState = validator.validate(state)

        // THEN the error becomes the four-digit variant and stays visible
        assertEquals(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_FOUR_DIGITS,
            validatedState.securityCode.error?.message,
        )
        assertTrue(validatedState.securityCode.error?.isVisible == true)

        // WHEN the brand is no longer detected
        state = validatedState.copy(detectedCardType = null)
        validatedState = validator.validate(state)

        // THEN the error falls back to the three-digit variant and stays visible
        assertEquals(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            validatedState.securityCode.error?.message,
        )
        assertTrue(validatedState.securityCode.error?.isVisible == true)
    }

    private fun createValidState() = StoredCardComponentState(
        securityCode = TextInputComponentState(text = "123"),
        isLoading = false,
        detectedCardType = createDetectedCardType(),
    )

    private fun createDetectedCardType(
        cardBrand: CardBrand = CardBrand("visa"),
    ) = DetectedCardType(
        cardBrand = cardBrand,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        isHidden = false,
        isShopperSelectionAllowedInDualBranded = false,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )

    companion object {

        @JvmStatic
        fun securityCodeErrorSource() = listOf(
            // text, requirementPolicy, cardBrand, expectedError
            // required empty
            arguments(
                "",
                RequirementPolicy.Required,
                null,
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_EMPTY_THREE_DIGITS,
            ),
            arguments(
                "  ",
                RequirementPolicy.Required,
                CardBrand("visa"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_EMPTY_THREE_DIGITS,
            ),
            arguments(
                "",
                RequirementPolicy.Required,
                CardBrand("amex"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_EMPTY_FOUR_DIGITS,
            ),
            // required incomplete
            arguments(
                "1",
                RequirementPolicy.Required,
                null,
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            ),
            arguments(
                "12",
                RequirementPolicy.Required,
                CardBrand("visa"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            ),
            arguments(
                "123",
                RequirementPolicy.Required,
                CardBrand("amex"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_FOUR_DIGITS,
            ),
            arguments(
                "12345",
                RequirementPolicy.Required,
                CardBrand("visa"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            ),
            // valid
            arguments("123", RequirementPolicy.Required, null, null),
            arguments("123", RequirementPolicy.Required, CardBrand("visa"), null),
            arguments("1234", RequirementPolicy.Required, CardBrand("amex"), null),
            // optional
            arguments("", RequirementPolicy.Optional, CardBrand("visa"), null),
            arguments(
                "1",
                RequirementPolicy.Optional,
                CardBrand("visa"),
                CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            ),
            // hidden, including bad content
            arguments("", RequirementPolicy.Hidden, CardBrand("visa"), null),
            arguments("not a code", RequirementPolicy.Hidden, CardBrand("amex"), null),
        )
    }
}
