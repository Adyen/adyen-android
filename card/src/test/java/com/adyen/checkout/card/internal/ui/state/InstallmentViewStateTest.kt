/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 18/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class InstallmentViewStateTest {

    @Test
    fun `when installmentOptions is empty, then toViewState returns null`() {
        // GIVEN
        val state = InstallmentState(
            installmentOptions = emptyList(),
            selectedInstallment = null,
        )

        // WHEN
        val result = state.toViewState()

        // THEN
        assertNull(result)
    }

    @Test
    fun `when installmentOptions is non-empty and selectedInstallment is set, then toViewState returns correct ViewState`() {
        // GIVEN
        val options = listOf(
            InstallmentModel.Regular(numberOfInstallments = 3, amountPerInstallment = null, showAmount = false),
            InstallmentModel.Regular(numberOfInstallments = 6, amountPerInstallment = null, showAmount = false),
        )
        val selection = options.first()
        val state = InstallmentState(
            installmentOptions = options,
            selectedInstallment = selection,
        )

        // WHEN
        val result = state.toViewState()

        // THEN
        assertEquals(options, result?.installmentOptions)
        assertEquals(selection, result?.selectedInstallment)
    }

    @Test
    fun `when installmentOptions is non-empty and selectedInstallment is null, then toViewState returns ViewState with null selection`() {
        // GIVEN
        val options = listOf(
            InstallmentModel.Regular(numberOfInstallments = 2, amountPerInstallment = null, showAmount = false),
        )
        val state = InstallmentState(
            installmentOptions = options,
            selectedInstallment = null,
        )

        // WHEN
        val result = state.toViewState()

        // THEN
        assertEquals(options, result?.installmentOptions)
        assertNull(result?.selectedInstallment)
    }
}
