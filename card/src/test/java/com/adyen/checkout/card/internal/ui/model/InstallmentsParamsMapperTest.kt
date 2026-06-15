/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 15/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InstallmentsParamsMapperTest {

    @Test
    fun `when InstallmentConfiguration is mapped, then InstallmentParams has same values`() {
        val mcBrand = CardBrand("mc")
        val configuration = InstallmentConfiguration(
            defaultOptions = InstallmentOptions(
                values = listOf(2, 3),
                plans = listOf(InstallmentOptions.Plan.REGULAR),
                preselectedValue = 2
            ),
            cardBasedOptions = mapOf(
                mcBrand to InstallmentOptions(
                    values = listOf(3, 4),
                    plans = listOf(InstallmentOptions.Plan.REGULAR, InstallmentOptions.Plan.REVOLVING),
                    preselectedValue = 3
                )
            ),
            showInstallmentAmount = true
        )

        val params = configuration.mapToInstallmentParams(amount)

        assertEquals(listOf(2, 3), params.defaultOptions?.values)
        assertEquals(listOf(InstallmentPlan.REGULAR), params.defaultOptions?.plans)
        assertEquals(2, params.defaultOptions?.preselectedValue)

        val mcOptions = params.cardBasedOptions[mcBrand]
        assertEquals(listOf(3, 4), mcOptions?.values)
        assertEquals(listOf(InstallmentPlan.REGULAR, InstallmentPlan.REVOLVING), mcOptions?.plans)
        assertEquals(3, mcOptions?.preselectedValue)

        assertEquals(true, params.showInstallmentAmount)
    }

    @Test
    fun `when SessionInstallmentConfiguration is mapped, then InstallmentParams has same values`() {
        val sessionConfig = SessionInstallmentConfiguration(
            installmentOptions = mapOf(
                "card" to SessionInstallmentOptionsParams(
                    values = listOf(2, 3),
                    plans = listOf("regular"),
                    preselectedValue = 2
                ),
                "visa" to SessionInstallmentOptionsParams(
                    values = listOf(3, 4),
                    plans = listOf("regular", "revolving"),
                    preselectedValue = 3
                )
            ),
            showInstallmentAmount = true
        )

        val params = sessionConfig.mapToInstallmentParams(amount)

        assertEquals(listOf(2, 3), params.defaultOptions?.values)
        assertEquals(listOf(InstallmentPlan.REGULAR), params.defaultOptions?.plans)
        assertEquals(2, params.defaultOptions?.preselectedValue)

        val visaOptions = params.cardBasedOptions[CardBrand("visa")]
        assertEquals(listOf(3, 4), visaOptions?.values)
        assertEquals(listOf(InstallmentPlan.REGULAR, InstallmentPlan.REVOLVING), visaOptions?.plans)
        assertEquals(3, visaOptions?.preselectedValue)

        assertEquals(true, params.showInstallmentAmount)
    }
}
