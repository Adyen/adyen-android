/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.PickerInputComponentState
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

internal class CardComponentStateValidatorTest {

    private lateinit var validator: CardComponentStateValidator

    @BeforeEach
    fun beforeEach() {
        validator = CardComponentStateValidator(
            cardValidationMapper = CardValidationMapper(),
        )
    }

    @Test
    fun `when all fields are valid, then isValid returns true`() {
        val state = createValidState()

        val result = validator.isValid(state)

        assertTrue(result)
    }

    @Test
    fun `when card number is invalid, then isValid returns false`() {
        val state = createValidState().copy(
            cardNumber = TextInputComponentState(text = "123"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.cardNumber.error?.message)
    }

    @Test
    fun `when expiry date is invalid, then isValid returns false`() {
        val state = createValidState().copy(
            expiryDate = TextInputComponentState(text = "1320"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.expiryDate.error?.message)
    }

    @Test
    fun `when security code is invalid, then isValid returns false`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "1"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.securityCode.error?.message)
    }

    @Test
    fun `when holder name is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            holderName = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Required),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.holderName.error?.message)
    }

    @Test
    fun `when holder name is not required and empty, then isValid returns true`() {
        val state = createValidState().copy(
            holderName = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Hidden),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.holderName.error?.message)
    }

    @Test
    fun `when social security number is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            socialSecurityNumber = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Required),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.socialSecurityNumber.error?.message)
    }

    @Test
    fun `when social security number is required and invalid, then isValid returns false`() {
        val state = createValidState().copy(
            socialSecurityNumber = TextInputComponentState(
                text = "123",
                requirementPolicy = RequirementPolicy.Required,
            ),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.socialSecurityNumber.error?.message)
    }

    @Test
    fun `when social security number is not required and empty, then isValid returns true`() {
        val state = createValidState().copy(
            socialSecurityNumber = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Hidden),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.socialSecurityNumber.error?.message)
    }

    @Test
    fun `when kcp birth date or tax number is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            kcpBirthDateOrTaxNumber = TextInputComponentState(
                text = "",
                requirementPolicy = RequirementPolicy.Required,
            ),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.kcpBirthDateOrTaxNumber.error?.message)
    }

    @Test
    fun `when kcp birth date or tax number is required and invalid, then isValid returns false`() {
        val state = createValidState().copy(
            kcpBirthDateOrTaxNumber = TextInputComponentState(
                text = "123",
                requirementPolicy = RequirementPolicy.Required,
            ),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.kcpBirthDateOrTaxNumber.error?.message)
    }

    @Test
    fun `when kcp birth date or tax number is not required and empty, then isValid returns true`() {
        val state = createValidState().copy(
            kcpBirthDateOrTaxNumber = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Hidden),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.kcpBirthDateOrTaxNumber.error?.message)
    }

    @Test
    fun `when kcp card password is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            kcpCardPassword = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Required),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.kcpCardPassword.error?.message)
    }

    @Test
    fun `when kcp card password is required and invalid, then isValid returns false`() {
        val state = createValidState().copy(
            kcpCardPassword = TextInputComponentState(
                text = "123",
                requirementPolicy = RequirementPolicy.Required,
            ),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.kcpCardPassword.error?.message)
    }

    @Test
    fun `when kcp card password is not required and empty, then isValid returns true`() {
        val state = createValidState().copy(
            kcpCardPassword = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Hidden),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.kcpCardPassword.error?.message)
    }

    @Test
    fun `when postal code is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            postalCode = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Required),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.postalCode.error?.message)
    }

    @Test
    fun `when postal code is required and too long, then isValid returns false`() {
        val state = createValidState().copy(
            postalCode = TextInputComponentState(
                text = "12345678901",
                requirementPolicy = RequirementPolicy.Required,
            ),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.postalCode.error?.message)
    }

    @Test
    fun `when postal code is optional and empty, then isValid returns true`() {
        val state = createValidState().copy(
            postalCode = TextInputComponentState(text = "", requirementPolicy = RequirementPolicy.Optional),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.postalCode.error?.message)
    }

    @Test
    fun `when card brand is unsupported, then card number has error`() {
        val state = createValidState().copy(
            cardBrandState = CardBrandState.UnsupportedBrand,
        )

        val validatedState = validator.validate(state)

        assertNotNull(validatedState.cardNumber.error?.message)
    }

    @Test
    fun `when the billing address row is visible and nothing is confirmed then it has an error`() {
        // GIVEN
        val state = createValidState().copy(
            billingAddress = BillingAddressState(isVisible = true, address = PickerInputComponentState()),
        )

        // WHEN
        val validatedState = validator.validate(state)

        // THEN
        assertEquals(
            CheckoutLocalizationKey.CARD_BILLING_ADDRESS_ERROR,
            validatedState.billingAddress.address.error?.message,
        )
    }

    @Test
    fun `when an address is confirmed then the billing address row is valid`() {
        // GIVEN
        val address = AddressModel(
            country = "NL",
            street = "Simon Carmiggeltstraat",
            houseNumberOrName = "6",
            postalCode = "1011 DJ",
            city = "Amsterdam",
            stateOrProvince = null,
        )
        val state = createValidState().copy(
            billingAddress = BillingAddressState(isVisible = true, address = PickerInputComponentState(address)),
        )

        // WHEN
        val validatedState = validator.validate(state)

        // THEN
        assertNull(validatedState.billingAddress.address.error)
    }

    @Test
    fun `when the billing address row is hidden then it has no error`() {
        // GIVEN
        val state = createValidState().copy(
            billingAddress = BillingAddressState(isVisible = false, address = PickerInputComponentState()),
        )

        // WHEN
        val validatedState = validator.validate(state)

        // THEN
        assertNull(validatedState.billingAddress.address.error)
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
            cardBrandState = cardBrand?.let { CardBrandState.SingleReliableBrand(createCardBrandData(it)) }
                ?: CardBrandState.NoBrandsDetected,
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
            cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData(CardBrand("visa"))),
        )

        // WHEN the brand changes to Amex
        state = state.copy(
            cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData(CardBrand.AMERICAN_EXPRESS)),
        )
        var validatedState = validator.validate(state)

        // THEN the error becomes the four-digit variant and stays visible
        assertEquals(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_FOUR_DIGITS,
            validatedState.securityCode.error?.message,
        )
        assertTrue(validatedState.securityCode.error?.isVisible == true)

        // WHEN the brand is no longer detected
        state = validatedState.copy(cardBrandState = CardBrandState.NoBrandsDetected)
        validatedState = validator.validate(state)

        // THEN the error falls back to the three-digit variant and stays visible
        assertEquals(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_ERROR_INCOMPLETE_THREE_DIGITS,
            validatedState.securityCode.error?.message,
        )
        assertTrue(validatedState.securityCode.error?.isVisible == true)
    }

    private fun createCardBrandData(cardBrand: CardBrand) = CardBrandData(
        cardBrand = cardBrand,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )

    private fun createValidState() = CardComponentState(
        cardNumber = TextInputComponentState(text = "4111111111111111"),
        expiryDate = TextInputComponentState(text = "1230"),
        securityCode = TextInputComponentState(text = "123"),
        holderName = TextInputComponentState(text = "John Doe", requirementPolicy = RequirementPolicy.Hidden),
        socialSecurityNumber = TextInputComponentState(
            text = "12312312312",
            requirementPolicy = RequirementPolicy.Hidden,
        ),
        kcpBirthDateOrTaxNumber = TextInputComponentState(text = "260403"),
        kcpCardPassword = TextInputComponentState(text = "12"),
        postalCode = TextInputComponentState(text = "1234 AB", requirementPolicy = RequirementPolicy.Required),
        billingAddress = BillingAddressState(isVisible = false, address = PickerInputComponentState()),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        showSupportedCardBrandLogos = true,
        isLoading = false,
        isCardScanningAvailable = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
        networkBinLookupState = null,
        installmentState = InstallmentState(
            installmentOptions = emptyList(),
            selectedInstallment = null,
        ),
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
