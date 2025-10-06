/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 24/2/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.old.InstallmentConfiguration
import com.adyen.checkout.card.old.InstallmentOptions
import com.adyen.checkout.card.old.internal.ui.model.InstallmentOptionParams
import com.adyen.checkout.card.old.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.old.internal.ui.model.InstallmentsParamsMapper
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class InstallmentParamsMapperTest {

    private val installmentsParamsMapper: InstallmentsParamsMapper = InstallmentsParamsMapper()
    private val amount = Amount("EUR", 100)
    private val shopperLocale = Locale.US
    private val showInstallmentAmount = true

    @Test
    fun `when session setup installment option is default then installment params should be the same `() {
        val sessionSetupInstallmentOptionsMap = SessionInstallmentConfiguration(
            installmentOptions = mapOf(
                DEFAULT_INSTALLMENT_OPTION to SessionInstallmentOptionsParams(
                    plans = listOf(INSTALLMENT_PLAN),
                    preselectedValue = 2,
                    values = listOf(2),
                ),
            ),
            showInstallmentAmount = showInstallmentAmount,
        )
        val expectedInstallmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false,
            ),
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(
            installmentConfiguration = sessionSetupInstallmentOptionsMap,
            amount = amount,
            shopperLocale = shopperLocale,
        )

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session setup installment option is card based then installment params should be the same `() {
        val sessionSetupInstallmentOptionsMap = SessionInstallmentConfiguration(
            installmentOptions = mapOf(
                CardType.VISA.txVariant to SessionInstallmentOptionsParams(
                    plans = listOf(INSTALLMENT_PLAN),
                    preselectedValue = 2,
                    values = listOf(2),
                ),
            ),
            showInstallmentAmount = showInstallmentAmount,
        )
        val expectedInstallmentParams = InstallmentParams(
            cardBasedOptions = listOf(
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA),
                ),
            ),
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(
            installmentConfiguration = sessionSetupInstallmentOptionsMap,
            amount = amount,
            shopperLocale = shopperLocale,
        )

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session installment configuration is default then installment params should be the same`() {
        val installmentConfiguration = InstallmentConfiguration(
            defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false,
            ),
            showInstallmentAmount = showInstallmentAmount,
        )

        val expectedInstallmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(2),
                includeRevolving = false,
            ),
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(
            installmentConfiguration = installmentConfiguration,
            amount = amount,
            shopperLocale = shopperLocale,
        )

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    @Test
    fun `when session installment configuration is card based then installment params should be the same`() {
        val installmentConfiguration = InstallmentConfiguration(
            cardBasedOptions = listOf(
                InstallmentOptions.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA),
                ),
            ),
            showInstallmentAmount = showInstallmentAmount,
        )

        val expectedInstallmentParams = InstallmentParams(
            cardBasedOptions = listOf(
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = listOf(2),
                    includeRevolving = false,
                    cardBrand = CardBrand(CardType.VISA),
                ),
            ),
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )

        val actualInstallmentParams = installmentsParamsMapper.mapToInstallmentParams(
            installmentConfiguration = installmentConfiguration,
            amount = amount,
            shopperLocale = shopperLocale,
        )

        assertEquals(expectedInstallmentParams, actualInstallmentParams)
    }

    companion object {
        private const val DEFAULT_INSTALLMENT_OPTION = "card"
        private const val INSTALLMENT_PLAN = "regular"
    }
}
