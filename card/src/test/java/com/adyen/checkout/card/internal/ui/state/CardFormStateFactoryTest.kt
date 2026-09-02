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
import org.mockito.kotlin.mock

/**
 * Tests `CardFormStateFactory`: which elements the card form shows, in which order, and whether each is valid.
 *
 * The order is the single source of truth for the layout, the keyboard action of each field, and which field receives
 * focus when the shopper presses pay, so a field missing from it is a field missing from the screen.
 */
internal class CardFormStateFactoryTest {

    @Nested
    inner class CanonicalOrderTest {

        /**
         * A field left out of the canonical order can never be displayed, whatever its configuration says.
         */
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
    inner class VisibleCardFieldsTest {

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
            // GIVEN
            val state = createState()

            // WHEN
            val order = state.form.elements.map { it.id }

            // THEN
            assertEquals(
                listOf(CardFormElementId.CARD_NUMBER, CardFormElementId.EXPIRY_DATE, CardFormElementId.SECURITY_CODE),
                order,
            )
        }

        /**
         * The security code is hidden for some brands, which is the one visibility change that happens while the
         * shopper is typing rather than at configuration time.
         */
        @Test
        fun `when the security code is hidden, then the fields around it keep their order`() {
            // GIVEN
            val state = createState(
                securityCode = hidden(),
                postalCode = required(),
            )

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
            // GIVEN
            val state = createState(holderName = optional())

            // WHEN
            val order = state.form.elements.map { it.id }

            // THEN
            assertTrue(order.contains(CardFormElementId.HOLDER_NAME))
        }

        @Test
        fun `when no installment options are available, then the installment picker is left out`() {
            // GIVEN
            val state = createState(installmentOptions = emptyList())

            // WHEN
            val order = state.form.elements.map { it.id }

            // THEN
            assertFalse(order.contains(CardFormElementId.INSTALLMENTS))
        }
    }

    /**
     * Each element reports whether it is valid, which is what decides the field focus moves to when the shopper presses
     * pay. The builder derives it per id, so a field wired to the wrong value would never hold up a payment.
     */
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
                // A field the builder wired to a constant, or to another field's value, would still report valid here.
                assertFalse(element.isValid, "$id holds an error but the element built for it reports valid")
            }
        }

        @Test
        fun `when every field holds a value, then no element is invalid`() {
            // GIVEN
            val state = createState(
                holderName = required(),
                postalCode = required(),
                isStorePaymentFieldVisible = true,
                installmentOptions = listOf(mock<InstallmentModel>()),
            )

            // WHEN
            val elements = state.form.elements

            // THEN
            assertTrue(elements.all { it.isValid }, "every field holds a value but an element reports invalid")
        }

        @Test
        fun `when the shopper has not reached a field that holds an error, then it is still invalid`() {
            // The shopper pressing pay is what surfaces these, so an error that is not yet on screen still counts.
            val state = createStateWithErrorOn(CardFormElementId.CARD_NUMBER)

            assertFalse(state.form.elements.first { it.id == CardFormElementId.CARD_NUMBER }.isValid)
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

    /**
     * The keyboard action of each field falls out of the order, so these are the cases that decide which field closes
     * the keyboard rather than moving on.
     */
    @Nested
    inner class KeyboardActionTest {

        @Test
        fun `when the last field is a text input, then it closes the keyboard`() {
            // GIVEN
            val state = createState(postalCode = required())

            // WHEN
            val keyboardAction = state.form.keyboardActionFor(CardFormElementId.POSTAL_CODE)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }

        @Test
        fun `when the store payment switch follows the last text input, then that field still closes the keyboard`() {
            // GIVEN
            val state = createState(postalCode = required(), isStorePaymentFieldVisible = true)

            // WHEN
            val keyboardAction = state.form.keyboardActionFor(CardFormElementId.POSTAL_CODE)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }

        @Test
        fun `when the security code is hidden, then the expiry date closes the keyboard`() {
            // GIVEN
            val state = createState(securityCode = hidden())

            // WHEN
            val keyboardAction = state.form.keyboardActionFor(CardFormElementId.EXPIRY_DATE)

            // THEN
            assertEquals(KeyboardAction.DONE, keyboardAction)
        }
    }

    /**
     * The order and what reaches the screen can no longer disagree, because the view state producer builds one element
     * per member of the order. What it can still get wrong is which element it builds for a given id: each element
     * reports its own id, and a wrong one would give the field the wrong composition key and send its focus events to
     * another field.
     */
    @Nested
    inner class AgreementWithViewStateTest {

        private val producer = CardViewStateProducer(amount = null, showSubmitButton = false)

        @Test
        fun `when every field is configured, then every element reports the id it was built for`() {
            assertElementsMatchOrder(
                createState(
                    holderName = required(),
                    socialSecurityNumber = required(),
                    kcpBirthDateOrTaxNumber = required(),
                    kcpCardPassword = required(),
                    postalCode = required(),
                    isStorePaymentFieldVisible = true,
                    installmentOptions = listOf(InstallmentModel.OneTime),
                ),
            )
        }

        @Test
        fun `when only the mandatory fields are configured, then every element reports the id it was built for`() {
            assertElementsMatchOrder(createState())
        }

        @Test
        fun `when the security code is hidden, then every element reports the id it was built for`() {
            assertElementsMatchOrder(createState(securityCode = hidden()))
        }

        @Test
        fun `when a field is optional, then every element reports the id it was built for`() {
            assertElementsMatchOrder(createState(holderName = optional()))
        }

        private fun assertElementsMatchOrder(state: CardComponentState) {
            val order = state.form.elements.map { it.id }

            val elements = producer.produce(state).elements

            assertEquals(order, elements.map { it.id })
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
