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
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        assertNotNull(validatedState.cardNumber.errorMessage)
    }

    @Test
    fun `when expiry date is invalid, then isValid returns false`() {
        val state = createValidState().copy(
            expiryDate = TextInputComponentState(text = "13/20"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.expiryDate.errorMessage)
    }

    @Test
    fun `when security code is invalid, then isValid returns false`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = "1"),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.securityCode.errorMessage)
    }

    @Test
    fun `when holder name is required and empty, then isValid returns false`() {
        val state = createValidState().copy(
            holderName = TextInputComponentState(text = ""),
            isHolderNameRequired = true,
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.holderName.errorMessage)
    }

    @Test
    fun `when holder name is not required and empty, then isValid returns true`() {
        val state = createValidState().copy(
            holderName = TextInputComponentState(text = ""),
            isHolderNameRequired = false,
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertTrue(result)
        assertNull(validatedState.holderName.errorMessage)
    }

    @Test
    fun `when card brand is unsupported and reliable, then card number has error`() {
        val state = createValidState().copy(
            detectedCardTypes = listOf(
                createDetectedCardType(isSupported = false, isReliable = true),
            ),
        )

        val validatedState = validator.validate(state)

        assertNotNull(validatedState.cardNumber.errorMessage)
    }

    private fun createValidState() = CardComponentState(
        cardNumber = TextInputComponentState(text = "4111111111111111"),
        expiryDate = TextInputComponentState(text = "12/30"),
        securityCode = TextInputComponentState(text = "123"),
        holderName = TextInputComponentState(text = "John Doe"),
        isHolderNameRequired = false,
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        detectedCardTypes = listOf(createDetectedCardType()),
        selectedCardBrand = null,
    )

    private fun createDetectedCardType(
        isSupported: Boolean = true,
        isReliable: Boolean = true,
    ) = DetectedCardType(
        cardBrand = CardBrand("visa"),
        isReliable = isReliable,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = isSupported,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}
