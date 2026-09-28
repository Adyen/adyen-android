/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.isErrorVisible
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * The post processor runs after the validator, so every state here is written as it would be once validation has
 * run: a field that should count as unfilled carries an error.
 */
internal class CardComponentStatePostProcessorTest {

    private val postProcessor = CardComponentStatePostProcessor()

    @Test
    fun `when the initial state is created, then the first invalid field is asked for focus`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the card number is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = process(state, CardIntent.HighlightValidationErrors)

        assertTrue(actual.cardNumber.isErrorVisible)
        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and nothing is invalid, then no error shows and no focus is requested`() {
        val state = createInitialState()

        val actual = process(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.isErrorVisible)
        assertFalse(actual.expiryDate.isErrorVisible)
        assertFalse(actual.securityCode.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when pay is pressed with several invalid fields, then the first one in the form gets focus`() {
        val state = createInitialState().copy(
            expiryDate = invalidField(),
            securityCode = invalidField(),
        )

        val actual = process(state, CardIntent.HighlightValidationErrors)

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    /**
     * The bug this whole mechanism exists for: the focus that pay asks for used to arrive as a plain focus gain, which
     * hid the error it had just revealed. The field flashed an error and lost it again.
     */
    @Test
    fun `when the field pay focused reports the focus gain, then it keeps showing its error`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = process(state, CardIntent.HighlightValidationErrors)

        val actual = process(
            highlighted,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when the shopper taps a field showing an error, then the error is hidden`() {
        val state = createInitialState().copy(expiryDate = invalidField(isErrorVisible = true))

        val actual = process(
            state,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertFalse(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when an invalid field loses focus, then its error is shown`() {
        val state = createInitialState().copy(expiryDate = invalidField())

        val actual = process(
            state,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = false),
        )

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    /**
     * A focus change never clears the request, because the UI reports back even when focus did not move - which is
     * what happens when pay targets a field that already has focus.
     */
    @Test
    fun `when the requested field gains focus, then the request is left for the UI to report back`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = process(state, CardIntent.HighlightValidationErrors)

        val actual = process(
            highlighted,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when the UI reports the request back, then it is cleared`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = process(state, CardIntent.HighlightValidationErrors)

        val actual = process(
            highlighted,
            CardIntent.FocusRequestConsumed(CardFormElementId.EXPIRY_DATE),
        )

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the UI reports back a request for another field, then the pending one is left alone`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = process(state, CardIntent.HighlightValidationErrors)

        val actual = process(
            highlighted,
            CardIntent.FocusRequestConsumed(CardFormElementId.CARD_NUMBER),
        )

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    @Nested
    inner class CardNumberAutoAdvanceTest {

        @Test
        fun `when card number becomes complete and valid, then the first invalid input after it is requested`() {
            val previousState = createInitialState().copy(
                cardNumber = invalidField(text = INCOMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = singleReliableBrand(),
            )
            val currentState = previousState.copy(cardNumber = validField(COMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(COMPLETE_PAN))

            assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
        }

        @Test
        fun `when bin lookup makes the card number complete, then the first remaining invalid input is requested`() {
            val previousState = createInitialState().copy(
                cardNumber = validField(COMPLETE_PAN),
                expiryDate = validField(COMPLETE_EXPIRY_DATE),
                securityCode = invalidField(),
            )
            val currentState = previousState.copy(cardBrandState = singleReliableBrand())

            val actual = process(previousState, currentState, detectedCardTypesUpdated())

            assertEquals(FocusRequest(CardFormElementId.SECURITY_CODE), actual.focusRequest)
        }

        @Test
        fun `when card number matches the first dual brand pan length, then focus is requested`() {
            val firstBrand = cardBrandData(panLength = COMPLETE_PAN.length)
            val secondBrand = cardBrandData(panLength = COMPLETE_PAN.length + 2)
            val previousState = createInitialState().copy(
                cardNumber = invalidField(text = INCOMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = CardBrandState.DualBrand(listOf(firstBrand, secondBrand)),
            )
            val currentState = previousState.copy(cardNumber = validField(COMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(COMPLETE_PAN))

            assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
        }

        @Test
        fun `when card number matches the selected dual brand pan length, then focus is requested`() {
            val firstBrand = cardBrandData(panLength = COMPLETE_PAN.length + 2)
            val selectedBrand = cardBrandData(panLength = COMPLETE_PAN.length)
            val previousState = createInitialState().copy(
                cardNumber = invalidField(text = INCOMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                    cardBrandDataList = listOf(firstBrand, selectedBrand),
                    shopperSelectedCardBrandData = selectedBrand,
                ),
            )
            val currentState = previousState.copy(cardNumber = validField(COMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(COMPLETE_PAN))

            assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
        }

        @Test
        fun `when bin lookup has no pan length, then focus is not requested`() {
            val previousState = createInitialState().copy(
                cardNumber = invalidField(text = INCOMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = singleReliableBrand(panLength = null),
            )
            val currentState = previousState.copy(cardNumber = validField(COMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(COMPLETE_PAN))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when card number is valid but incomplete, then focus is not requested`() {
            val previousState = createInitialState().copy(
                cardNumber = invalidField(),
                expiryDate = invalidField(),
                cardBrandState = singleReliableBrand(),
            )
            val currentState = previousState.copy(cardNumber = validField(INCOMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(INCOMPLETE_PAN))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when card number reaches pan length but is invalid, then focus is not requested`() {
            val previousState = createInitialState().copy(
                cardNumber = invalidField(text = INCOMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = singleReliableBrand(),
            )
            val currentState = previousState.copy(cardNumber = invalidField(text = COMPLETE_PAN))

            val actual = process(previousState, currentState, CardIntent.UpdateCardNumber(COMPLETE_PAN))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when complete card number stays complete, then focus is not requested again`() {
            val state = createInitialState().copy(
                cardNumber = validField(COMPLETE_PAN),
                expiryDate = invalidField(),
                cardBrandState = singleReliableBrand(),
            )

            val actual = process(state, state, detectedCardTypesUpdated())

            assertNull(actual.focusRequest)
        }
    }

    @Nested
    inner class ExpiryDateAutoAdvanceTest {

        @Test
        fun `when expiry date becomes complete and valid, then the first invalid input after it is requested`() {
            val previousState = createInitialState().copy(
                expiryDate = invalidField(text = INCOMPLETE_EXPIRY_DATE),
                securityCode = invalidField(),
            )
            val currentState = previousState.copy(expiryDate = validField(COMPLETE_EXPIRY_DATE))

            val actual = process(previousState, currentState, CardIntent.UpdateExpiryDate(COMPLETE_EXPIRY_DATE))

            assertEquals(FocusRequest(CardFormElementId.SECURITY_CODE), actual.focusRequest)
        }

        @Test
        fun `when hidden and valid inputs follow expiry date, then the next invalid input is requested`() {
            val previousState = createInitialState().copy(
                expiryDate = invalidField(text = INCOMPLETE_EXPIRY_DATE),
                securityCode = hiddenField(),
                socialSecurityNumber = invalidField(),
            )
            val currentState = previousState.copy(expiryDate = validField(COMPLETE_EXPIRY_DATE))

            val actual = process(previousState, currentState, CardIntent.UpdateExpiryDate(COMPLETE_EXPIRY_DATE))

            assertEquals(FocusRequest(CardFormElementId.SOCIAL_SECURITY_NUMBER), actual.focusRequest)
        }

        @Test
        fun `when no invalid input follows a completed expiry date, then focus is not requested`() {
            val previousState = createInitialState().copy(
                expiryDate = invalidField(text = INCOMPLETE_EXPIRY_DATE),
            )
            val currentState = previousState.copy(expiryDate = validField(COMPLETE_EXPIRY_DATE))

            val actual = process(previousState, currentState, CardIntent.UpdateExpiryDate(COMPLETE_EXPIRY_DATE))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when expiry date is complete but invalid, then focus is not requested`() {
            val previousState = createInitialState().copy(
                expiryDate = invalidField(text = INCOMPLETE_EXPIRY_DATE),
                securityCode = invalidField(),
            )
            val currentState = previousState.copy(expiryDate = invalidField(text = COMPLETE_EXPIRY_DATE))

            val actual = process(previousState, currentState, CardIntent.UpdateExpiryDate(COMPLETE_EXPIRY_DATE))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when expiry date is valid but incomplete, then focus is not requested`() {
            val previousState = createInitialState().copy(
                expiryDate = invalidField(),
                securityCode = invalidField(),
            )
            val currentState = previousState.copy(expiryDate = validField(INCOMPLETE_EXPIRY_DATE))

            val actual = process(previousState, currentState, CardIntent.UpdateExpiryDate(INCOMPLETE_EXPIRY_DATE))

            assertNull(actual.focusRequest)
        }

        @Test
        fun `when complete expiry date stays complete, then focus is not requested again`() {
            val state = createInitialState().copy(
                expiryDate = validField(COMPLETE_EXPIRY_DATE),
                securityCode = invalidField(),
            )

            val actual = process(state, state, CardIntent.UpdateExpiryDate(COMPLETE_EXPIRY_DATE))

            assertNull(actual.focusRequest)
        }
    }

    @Test
    fun `when a scan filled the card number and expiry date, then focus is requested on the security code`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = process(state, scanResult())

        assertEquals(FocusRequest(CardFormElementId.SECURITY_CODE), actual.focusRequest)
    }

    @Test
    fun `when a scan filled everything the form shows, then no focus is requested`() {
        val state = createInitialState()

        val actual = process(state, scanResult())

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the security code is hidden, then a scan sends focus to the next visible field`() {
        val state = createInitialState().copy(
            securityCode = hiddenField(),
            holderName = invalidField(),
        )

        val actual = process(state, scanResult())

        assertEquals(FocusRequest(CardFormElementId.HOLDER_NAME), actual.focusRequest)
    }

    /**
     * A scan can come back with only part of a card. Skipping to the field after the expiry date would then send the
     * shopper past a field the scan did not fill.
     */
    @Test
    fun `when a scan left the expiry date empty, then focus is requested on the expiry date`() {
        val state = createInitialState().copy(
            expiryDate = invalidField(),
            securityCode = invalidField(),
        )

        val actual = process(state, scanResult(expiryMonth = null, expiryYear = null))

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
    }

    /**
     * A scan without a card number also wipes the one the shopper had typed, so the card number is what needs filling
     * again. Reading only the expiry date and jumping forward to the security code would strand them.
     */
    @Test
    fun `when a scan left the card number empty, then focus is requested on the card number`() {
        val state = createInitialState().copy(
            cardNumber = invalidField(),
            securityCode = invalidField(),
        )

        val actual = process(state, scanResult(pan = null))

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    /**
     * Prefill behaves like a shopper tap, unlike the focus that pay asks for, so the field it lands on must not
     * surface an error the shopper has not seen yet.
     */
    @Test
    fun `when the field a scan focused was already showing an error, then the error is hidden on arrival`() {
        val state = createInitialState().copy(securityCode = invalidField(isErrorVisible = true))
        val scanned = process(state, scanResult())

        val actual = process(
            scanned,
            CardIntent.UpdateFieldFocus(CardFormElementId.SECURITY_CODE, hasFocus = true),
        )

        assertFalse(actual.securityCode.isErrorVisible)
    }

    @Test
    fun `when the intent decides no focus, then the state is untouched`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = process(state, CardIntent.UpdateLoading(true))

        assertEquals(state, actual)
    }

    private fun process(state: CardComponentState, intent: CardIntent) = process(state, state, intent)

    private fun process(
        previousState: CardComponentState,
        currentState: CardComponentState,
        intent: CardIntent,
    ) = postProcessor.process(previousState, currentState, intent)

    private fun detectedCardTypesUpdated() = CardIntent.UpdateDetectedCardTypes(
        DetectedCardTypeList(
            detectedCardTypes = emptyList(),
            source = DetectedCardTypeList.Source.NETWORK,
            cardDetectionBin = null,
            issuingCountryCode = null,
        ),
    )

    private fun singleReliableBrand(panLength: Int? = COMPLETE_PAN.length) =
        CardBrandState.SingleReliableBrand(cardBrandData(panLength))

    private fun cardBrandData(panLength: Int?) = CardBrandData(
        cardBrand = CardBrand("visa"),
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        panLength = panLength,
        paymentMethodVariant = null,
        localizedBrand = null,
    )

    private fun scanResult(
        pan: String? = "4111111111111111",
        expiryMonth: Int? = 12,
        expiryYear: Int? = 2025,
    ) = CardIntent.UpdateCardScanResult(pan = pan, expiryMonth = expiryMonth, expiryYear = expiryYear)

    private fun validField(text: String) = TextInputComponentState(text = text)

    private fun invalidField(text: String = "", isErrorVisible: Boolean = false) = TextInputComponentState(
        text = text,
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isErrorVisible),
    )

    private fun hiddenField() = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden)

    private fun createInitialState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        postalCode = TextInputComponentState(),
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

    private companion object {
        const val COMPLETE_PAN = "4000620000000007"
        const val INCOMPLETE_PAN = "400062000000000"
        const val COMPLETE_EXPIRY_DATE = "1230"
        const val INCOMPLETE_EXPIRY_DATE = "123"
    }
}
