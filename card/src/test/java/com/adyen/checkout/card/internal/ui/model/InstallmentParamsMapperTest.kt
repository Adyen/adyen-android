/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 24/2/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InstallmentParamsMapperTest {

    private val installmentsParamsMapper: InstallmentsParamsMapper = InstallmentsParamsMapper()

    @Test
    fun `when session setup installment option is default then installment params should be the same `() {
        val sessionSetupInstallmentOptionsMap = mapOf(
            DEFAULT_INSTALLMENT_OPTION to SessionInstallmentOptionsParams(
                plans = listOf(INSTALLMENT_PLAN),
                preselectedValue = 2,
                values = listOf(2)
            )
        )
        val expectedInstallmentParams = InstallmentParams(
            InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false
            )
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(sessionSetupInstallmentOptionsMap)

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session setup installment option is card based then installment params should be the same `() {
        val sessionSetupInstallmentOptionsMap = mapOf(
            CardType.VISA.txVariant to SessionInstallmentOptionsParams(
                plans = listOf(INSTALLMENT_PLAN),
                preselectedValue = 2,
                values = listOf(2)
            )
        )
        val expectedInstallmentParams = InstallmentParams(
            cardBasedOptions = listOf(
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA)
                )
            )
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(sessionSetupInstallmentOptionsMap)

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session installment configuration is default then installment params should be the same`() {
        val installmentConfiguration = InstallmentConfiguration(
            defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false
            )
        )

        val expectedInstallmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false
            )
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(installmentConfiguration)

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session installment configuration is card based then installment params should be the same`() {
        val installmentConfiguration = InstallmentConfiguration(
            cardBasedOptions = listOf(
                InstallmentOptions.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA)
                )
            )
        )

        val expectedInstallmentParams = InstallmentParams(
            cardBasedOptions = listOf(
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA)
                )
            )
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(installmentConfiguration)

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    companion object {
        private const val DEFAULT_INSTALLMENT_OPTION = "card"
        private const val INSTALLMENT_PLAN = "regular"
    }
}
