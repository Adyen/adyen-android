/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.DetectCardTypeBinHelper
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock

@ExtendWith(MockitoExtension::class)
internal class CardComponentStateReducerTest {

    private lateinit var reducer: CardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        val detectCardTypeBinHelper = DetectCardTypeBinHelper()
        val cardComponentParams = mock<CardComponentParams>()
        val cardBrandIntentsHandler = CardBrandIntentsHandler(cardComponentParams, detectCardTypeBinHelper)
        reducer = CardComponentStateReducer(cardBrandIntentsHandler)
    }

    @Test
    fun `when intent is UpdateCardNumber, then cardNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardNumber("4111111111111111"))

        assertEquals("4111111111111111", actual.cardNumber.text)
    }

    @Test
    fun `when intent is UpdateExpiryDate, then expiryDate state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDate("1225"))

        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateHolderName, then holderName state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderName("John Doe"))

        assertEquals("John Doe", actual.holderName.text)
    }

    @Test
    fun `when intent is UpdateSocialSecurityNumber, then socialSecurityNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSocialSecurityNumber("123456"))

        assertEquals("123456", actual.socialSecurityNumber.text)
    }

    @Test
    fun `when intent is UpdateKcpBirthDateOrTaxNumber, then kcpBirthDateOrTaxNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpBirthDateOrTaxNumber("123456"))

        assertEquals("123456", actual.kcpBirthDateOrTaxNumber.text)
    }

    @Test
    fun `when intent is UpdateKcpCardPassword, then kcpCardPassword state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpCardPassword("123456"))

        assertEquals("123456", actual.kcpCardPassword.text)
    }

    @Test
    fun `when intent is UpdatePostalCode, then postalCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdatePostalCode("1234 AB"))

        assertEquals("1234 AB", actual.postalCode.text)
    }

    @Test
    fun `when intent is UpdateStorePaymentMethod, then storePaymentMethod is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateStorePaymentMethod(true))

        assertTrue(actual.storePaymentMethod)
    }

    @Test
    fun `when intent is UpdateLoading, then isLoading is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is UpdateCardScanningAvailability with true, then isCardScanningAvailable is true`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardScanningAvailability(true))

        assertTrue(actual.isCardScanningAvailable)
    }

    @Test
    fun `when intent is UpdateCardScanningAvailability with false, then isCardScanningAvailable is false`() {
        val state = createInitialState().copy(isCardScanningAvailable = true)

        val actual = reducer.reduce(state, CardIntent.UpdateCardScanningAvailability(false))

        assertFalse(actual.isCardScanningAvailable)
    }

    @Test
    fun `when intent is UpdateCardScanResult with pan and expiry, then cardNumber and expiryDate are updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        assertEquals("4111111111111111", actual.cardNumber.text)
        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateCardScanResult with null pan, then cardNumber is empty`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = null, expiryMonth = 3, expiryYear = 2026),
        )

        assertEquals("", actual.cardNumber.text)
        assertEquals("0326", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateCardScanResult with null expiry, then expiryDate is empty`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "5500000000000004", expiryMonth = null, expiryYear = null),
        )

        assertEquals("5500000000000004", actual.cardNumber.text)
        assertEquals("", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateInstallment, then selectedInstallment is updated`() {
        val state = createInitialState()
        val installment = InstallmentModel.Regular(
            numberOfInstallments = 3,
            amountPerInstallment = null,
            showAmount = false,
        )

        val actual = reducer.reduce(state, CardIntent.UpdateInstallment(installment))

        assertEquals(installment, actual.installmentState.selectedInstallment)
    }

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
}
