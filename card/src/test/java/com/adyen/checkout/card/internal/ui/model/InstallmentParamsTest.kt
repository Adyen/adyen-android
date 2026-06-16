/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 15/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.data.model.Amount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InstallmentParamsTest {

    private val amount = Amount(currency = "EUR", value = 120)

    @Test
    fun `when installmentParams is empty, then mapToInstallmentModels returns empty list`() {
        val params = InstallmentParams(amount = amount)
        val models = params.mapToInstallmentModels()
        assertEquals(emptyList<InstallmentModel>(), models)
    }

    @Test
    fun `when installmentParams has defaultOptions, then mapToInstallmentModels returns options with OneTime first`() {
        val params = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(2, 3),
                plans = listOf(InstallmentPlan.REGULAR)
            ),
            amount = amount,
            showInstallmentAmount = true,
        )

        val models = params.mapToInstallmentModels()

        val expected = listOf(
            InstallmentModel.OneTime,
            InstallmentModel.Regular(2, Amount("EUR", 60), showAmount = true),
            InstallmentModel.Regular(3, Amount("EUR", 40), showAmount = true)
        )
        assertEquals(expected, models)
    }

    @Test
    fun `when installmentParams has defaultOptions with revolving, then mapToInstallmentModels returns revolving model`() {
        val params = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(3),
                plans = listOf(InstallmentPlan.REGULAR, InstallmentPlan.REVOLVING)
            ),
            amount = amount,
            showInstallmentAmount = false,
        )

        val models = params.mapToInstallmentModels()

        val expected = listOf(
            InstallmentModel.OneTime,
            InstallmentModel.Revolving,
            InstallmentModel.Regular(3, Amount("EUR", 40), showAmount = false)
        )
        assertEquals(expected, models)
    }

    @Test
    fun `when installmentParams has cardBasedOptions and brand matches, then it returns brand options`() {
        val visaBrand = CardBrand("visa")
        val mcBrand = CardBrand("mastercard")
        val params = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(2),
                plans = listOf(InstallmentPlan.REGULAR)
            ),
            cardBasedOptions = mapOf(
                visaBrand to InstallmentOptionsParams(
                    values = listOf(3, 4),
                    plans = listOf(InstallmentPlan.REGULAR)
                )
            ),
            amount = amount,
            showInstallmentAmount = true,
        )

        // Matching brand
        val visaModels = params.mapToInstallmentModels(visaBrand)
        val expectedVisa = listOf(
            InstallmentModel.OneTime,
            InstallmentModel.Regular(3, Amount("EUR", 40), showAmount = true),
            InstallmentModel.Regular(4, Amount("EUR", 30), showAmount = true)
        )
        assertEquals(expectedVisa, visaModels)

        // Non-matching brand falls back to default options
        val mcModels = params.mapToInstallmentModels(mcBrand)
        val expectedMc = listOf(
            InstallmentModel.OneTime,
            InstallmentModel.Regular(2, Amount("EUR", 60), showAmount = true)
        )
        assertEquals(expectedMc, mcModels)
    }

    @Test
    fun `when amount is null, then mapToInstallmentModels has null amountPerInstallment`() {
        val params = InstallmentParams(
            defaultOptions = InstallmentOptionsParams(
                values = listOf(2),
                plans = listOf(InstallmentPlan.REGULAR)
            ),
            amount = null,
            showInstallmentAmount = true,
        )

        val models = params.mapToInstallmentModels()

        val expected = listOf(
            InstallmentModel.OneTime,
            InstallmentModel.Regular(2, amountPerInstallment = null, showAmount = true)
        )
        assertEquals(expected, models)
    }
}
