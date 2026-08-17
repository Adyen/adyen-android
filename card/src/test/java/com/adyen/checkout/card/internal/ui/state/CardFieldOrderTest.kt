/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.components.internal.ui.state.form.lastTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests `CardFieldOrder.kt`: which fields the card form shows and in which order.
 *
 * The order is the single source of truth for the layout, the keyboard action of each field, and which field receives
 * focus when the shopper presses pay, so a field missing from it is a field missing from the screen.
 */
internal class CardFieldOrderTest {

    @Nested
    inner class CanonicalOrderTest {

        /**
         * A field left out of the canonical order can never be displayed, whatever its configuration says.
         */
        @Test
        fun `when the canonical order is read, then it contains every card field exactly once`() {
            // WHEN
            val order = canonicalCardFieldOrder()

            // THEN
            assertEquals(CardFieldId.entries.toSet(), order.toSet())
            assertEquals(CardFieldId.entries.size, order.size)
        }

        @Test
        fun `when the canonical order is read, then the card number comes first`() {
            // WHEN
            val order = canonicalCardFieldOrder()

            // THEN
            assertEquals(CardFieldId.CARD_NUMBER, order.first())
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
            val order = visibleCardFields(state)

            // THEN
            assertEquals(canonicalCardFieldOrder(), order)
        }

        @Test
        fun `when only the mandatory fields are configured, then the optional ones are left out`() {
            // GIVEN
            val state = createState()

            // WHEN
            val order = visibleCardFields(state)

            // THEN
            assertEquals(
                listOf(CardFieldId.CARD_NUMBER, CardFieldId.EXPIRY_DATE, CardFieldId.SECURITY_CODE),
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
            val order = visibleCardFields(state)

            // THEN
            assertEquals(
                listOf(CardFieldId.CARD_NUMBER, CardFieldId.EXPIRY_DATE, CardFieldId.POSTAL_CODE),
                order,
            )
        }

        @Test
        fun `when an optional field is configured, then it is shown`() {
            // GIVEN
            val state = createState(holderName = optional())

            // WHEN
            val order = visibleCardFields(state)

            // THEN
            assertTrue(order.contains(CardFieldId.HOLDER_NAME))
        }

        @Test
        fun `when no installment options are available, then the installment picker is left out`() {
            // GIVEN
            val state = createState(installmentOptions = emptyList())

            // WHEN
            val order = visibleCardFields(state)

            // THEN
            assertTrue(!order.contains(CardFieldId.INSTALLMENTS))
        }
    }

    /**
     * The keyboard action of each field falls out of the order, so these are the cases that decide which field closes
     * the keyboard rather than moving on.
     */
    @Nested
    inner class LastTextInputTest {

        @Test
        fun `when the last field is a text input, then it is the last text input`() {
            // GIVEN
            val state = createState(postalCode = required())

            // WHEN
            val lastTextInput = state.form.lastTextInput()

            // THEN
            assertEquals(CardFieldId.POSTAL_CODE, lastTextInput)
        }

        @Test
        fun `when the form ends with the store payment switch, then the last text input is the field before it`() {
            // GIVEN
            val state = createState(postalCode = required(), isStorePaymentFieldVisible = true)

            // WHEN
            val lastTextInput = state.form.lastTextInput()

            // THEN
            assertEquals(CardFieldId.POSTAL_CODE, lastTextInput)
        }

        @Test
        fun `when the security code is hidden, then the expiry date becomes the last text input`() {
            // GIVEN
            val state = createState(securityCode = hidden())

            // WHEN
            val lastTextInput = state.form.lastTextInput()

            // THEN
            assertEquals(CardFieldId.EXPIRY_DATE, lastTextInput)
        }
    }

    /**
     * The UI renders a field by looking up the matching property of the view state, so a field in the order whose
     * property is null would be silently dropped, and a field on screen that is not in the order would be unreachable
     * by focus and have the wrong keyboard action. These are the states in which those two can disagree.
     */
    @Nested
    inner class AgreementWithViewStateTest {

        private val producer = CardViewStateProducer(amount = null, showSubmitButton = false)

        @Test
        fun `when every field is configured, then the order agrees with the view state`() {
            assertOrderAgreesWithViewState(
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
        fun `when only the mandatory fields are configured, then the order agrees with the view state`() {
            assertOrderAgreesWithViewState(createState())
        }

        @Test
        fun `when the security code is hidden, then the order agrees with the view state`() {
            assertOrderAgreesWithViewState(createState(securityCode = hidden()))
        }

        @Test
        fun `when a field is optional, then the order agrees with the view state`() {
            assertOrderAgreesWithViewState(createState(holderName = optional()))
        }

        private fun assertOrderAgreesWithViewState(state: CardComponentState) {
            val order = visibleCardFields(state)
            val viewState = producer.produce(state)

            CardFieldId.entries.forEach { id ->
                val isDisplayed = when (id) {
                    CardFieldId.CARD_NUMBER -> viewState.cardNumber != null
                    CardFieldId.EXPIRY_DATE -> viewState.expiryDate != null
                    CardFieldId.SECURITY_CODE -> viewState.securityCode != null
                    CardFieldId.HOLDER_NAME -> viewState.holderName != null
                    CardFieldId.SOCIAL_SECURITY_NUMBER -> viewState.socialSecurityNumber != null
                    CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> viewState.kcpBirthDateOrTaxNumber != null
                    CardFieldId.KCP_CARD_PASSWORD -> viewState.kcpCardPassword != null
                    CardFieldId.POSTAL_CODE -> viewState.postalCode != null
                    CardFieldId.STORE_PAYMENT_METHOD -> viewState.storePaymentViewState != null
                    CardFieldId.INSTALLMENTS -> viewState.installmentViewState != null
                }

                assertEquals(isDisplayed, order.contains(id), "Order and view state disagree on $id")
            }
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
