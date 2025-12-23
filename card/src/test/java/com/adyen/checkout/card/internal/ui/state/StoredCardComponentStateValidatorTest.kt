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
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        assertNotNull(validatedState.securityCode.errorMessage)
    }

    @Test
    fun `when security code is empty, then isValid returns false`() {
        val state = createValidState().copy(
            securityCode = TextInputComponentState(text = ""),
        )

        val validatedState = validator.validate(state)
        val result = validator.isValid(validatedState)

        assertFalse(result)
        assertNotNull(validatedState.securityCode.errorMessage)
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
        assertNull(validatedState.securityCode.errorMessage)
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
        assertNull(validatedState.securityCode.errorMessage)
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
        isReliable = true,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}
