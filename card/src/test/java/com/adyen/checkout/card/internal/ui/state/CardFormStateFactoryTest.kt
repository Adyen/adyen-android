/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.form.keyboardActionFor
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardFormStateFactoryTest {

    @Nested
    inner class CanonicalOrderTest {

        @Test
        fun `when the canonical order is read, then it contains every card field exactly once`() {
            // WHEN
            val order = CardFormStateFactory.CANONICAL_ORDER

            // THEN
            assertEquals(CardFormElementId.entries.toSet(), order.toSet())
            assertEquals(CardFormElementId.entries.size, order.size)
        }

        @Test
        fun `when the canonical order is read, then the card number comes first`() {
            // WHEN
            val order = CardFormStateFactory.CANONICAL_ORDER

            // THEN
            assertEquals(CardFormElementId.CARD_NUMBER, order.first())
        }
    }

    @Nested
    inner class VisibleElementsTest {

        @Test
        fun `when every field is configured, then all of them are shown in the canonical order`() {
            // GIVEN
            val state = createState(
                holderName = required(),
                socialSecurityNumber = required(),
                kcpBirthDateOrTaxNumber = required(),
                kcpCardPassword = required(),
                postalCode = required(),
                isStorePaymentFieldVisible = true,
                installmentOptions = listOf(InstallmentModel.OneTime),
            )

            // WHEN
            val order = state.form.elements.map { it.id }

            // THEN
            assertEquals(CardFormStateFactory.CANONICAL_ORDER, order)
        }

        @Test
        fun `when only the mandatory fields are configured, then the optional ones are left out`() {
            // WHEN
            val order = createState().form.elements.map { it.id }

            // THEN
            assertEquals(
                listOf(CardFormElementId.CARD_NUMBER, CardFormElementId.EXPIRY_DATE, CardFormElementId.SECURITY_CODE),
                order,
            )
        }

        @Test
        fun `when the security code is hidden, then the fields around it keep their order`() {
            // GIVEN
            val state = createState(securityCode = hidden(), postalCode = required())

            // WHEN
            val order = state.form.elements.map { it.id }

            // THEN
            assertEquals(
                listOf(CardFormElementId.CARD_NUMBER, CardFormElementId.EXPIRY_DATE, CardFormElementId.POSTAL_CODE),
                order,
            )
        }

        @Test
        fun `when an optional field is configured, then it is shown`() {
            val state = createState(holderName = optional())

            assertTrue(state.form.elements.any { it.id == CardFormElementId.HOLDER_NAME })
        }

        @Test
        fun `when no installment options are available, then the installment picker is left out`() {
            assertFalse(createState().form.elements.any { it.id == CardFormElementId.INSTALLMENTS })
        }
    }

    @Nested
    inner class ValidityTest {

        @Test
        fun `when a text input holds an error, then the element built for it is invalid`() {
            CardFormElementId.entries.filter { it.isTextInput }.forEach { id ->
                // GIVEN
                val state = createStateWithErrorOn(id)

                // WHEN
                val element = state.form.elements.first { it.id == id }

                // THEN
                assertFalse(element.isValid, "$id holds an error but the element built for it reports valid")
            }
        }

        @Test
        fun `when every field holds a value, then no element is invalid`() {
            val state = createState(
                holderName = required(),
                postalCode = required(),
                isStorePaymentFieldVisible = true,
                installmentOptions = listOf(InstallmentModel.OneTime),
            )

            assertTrue(state.form.isFormValid)
        }

        @Test
        fun `when the shopper has not reached a field that holds an error, then it is still invalid`() {
            val state = createStateWithErrorOn(CardFormElementId.CARD_NUMBER)

            assertFalse(state.form.isElementVisibleAndValid(CardFormElementId.CARD_NUMBER))
            assertFalse(state.cardNumber.isErrorVisible)
        }

        private fun createStateWithErrorOn(id: CardFormElementId): CardComponentState {
            val state = createState(
                holderName = required(),
                socialSecurityNumber = required(),
                kcpBirthDateOrTaxNumber = required(),
                kcpCardPassword = required(),
                postalCode = required(),
            )
            return state.updateTextInput(id) { field -> field.updateError(CheckoutLocalizationKey.CARD_NUMBER_INVALID) }
        }
    }

    @Nested
    inner class KeyboardActionTest {

        @Test
        fun `when the last field is a text input, then it closes the keyboard`() {
            val state = createState(postalCode = required())

            assertEquals(KeyboardAction.DONE, state.form.keyboardActionFor(CardFormElementId.POSTAL_CODE))
        }

        @Test
        fun `when the store payment switch follows the last text input, then that field still closes the keyboard`() {
            val state = createState(postalCode = required(), isStorePaymentFieldVisible = true)

            assertEquals(KeyboardAction.DONE, state.form.keyboardActionFor(CardFormElementId.POSTAL_CODE))
        }

        @Test
        fun `when the security code is hidden, then the expiry date closes the keyboard`() {
            val state = createState(securityCode = hidden())

            assertEquals(KeyboardAction.DONE, state.form.keyboardActionFor(CardFormElementId.EXPIRY_DATE))
        }
    }

    @Suppress("LongParameterList")
    private fun createState(
        securityCode: TextInputComponentState = required(),
        holderName: TextInputComponentState = hidden(),
        socialSecurityNumber: TextInputComponentState = hidden(),
        kcpBirthDateOrTaxNumber: TextInputComponentState = hidden(),
        kcpCardPassword: TextInputComponentState = hidden(),
        postalCode: TextInputComponentState = hidden(),
        isStorePaymentFieldVisible: Boolean = false,
        installmentOptions: List<InstallmentModel> = emptyList(),
    ) = CardComponentState(
        cardNumber = required(),
        expiryDate = required(),
        securityCode = securityCode,
        holderName = holderName,
        socialSecurityNumber = socialSecurityNumber,
        kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumber,
        kcpCardPassword = kcpCardPassword,
        postalCode = postalCode,
        storePaymentMethod = false,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        supportedCardBrands = emptyList(),
        showSupportedCardBrandLogos = false,
        isLoading = false,
        isCardScanningAvailable = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
        networkBinLookupState = null,
        installmentState = InstallmentState(
            installmentOptions = installmentOptions,
            selectedInstallment = installmentOptions.firstOrNull(),
        ),
    )

    private fun required() = TextInputComponentState(requirementPolicy = RequirementPolicy.Required)

    private fun optional() = TextInputComponentState(requirementPolicy = RequirementPolicy.Optional)

    private fun hidden() = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden)
}
