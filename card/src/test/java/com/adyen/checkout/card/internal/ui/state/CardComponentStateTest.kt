/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardComponentStateTest {

    /**
     * `updateTextInput` spells out which property each field id names. An exhaustive `when` proves every id is handled
     * but not that each one is handled *correctly*, so these cover what it cannot.
     */
    @Nested
    inner class UpdateTextInputTest {

        @Test
        fun `when each text input is updated in turn, then each one writes a different field`() {
            // GIVEN
            val textInputIds = CardFormElementId.entries.filter { it.isTextInput }

            // WHEN
            val states = textInputIds.map { id -> createState().updateTextInput(id) { it.updateText(MARKER) } }

            // THEN
            // Two ids producing the same state would mean they write the same property, which is the one mistake the
            // compiler cannot catch here.
            assertEquals(textInputIds.size, states.distinct().size, "Two field ids write the same property")
        }

        @Test
        fun `when the field is not a text input, then there is nothing to update`() {
            CardFormElementId.entries.filterNot { it.isTextInput }.forEach { id ->
                // GIVEN
                val state = createState()

                // WHEN
                val updated = state.updateTextInput(id) { it.updateText(MARKER) }

                // THEN
                assertEquals(state, updated)
            }
        }
    }

    private fun createState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        postalCode = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList<CardBrand>(),
        showSupportedCardBrandLogos = false,
        isLoading = false,
        isCardScanningAvailable = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
        networkBinLookupState = null,
        installmentState = InstallmentState(emptyList(), null),
    )

    private companion object {
        private const val MARKER = "written by the lens"
    }
}
