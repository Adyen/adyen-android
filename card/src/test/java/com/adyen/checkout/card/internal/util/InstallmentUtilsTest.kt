/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2023.
 */

package com.adyen.checkout.card.internal.util

import android.content.Context
import androidx.annotation.StringRes
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.model.InstallmentOptionParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.util.formatToLocalizedString
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

internal class InstallmentUtilsTest {

    private val context = mock<Context>().apply {
        whenever(getString(any())).thenReturn("Some text")
        whenever(getString(any(), any())).thenReturn("Some text")
        whenever(getString(any(), any(), any())).thenReturn("Some text")
    }

    @ParameterizedTest
    @MethodSource("noValidInstallmentsSourceForMakeInstallmentOptions")
    fun `make installment options returns empty list, when there are no valid installment options`(
        params: InstallmentParams?,
        cardBrand: CardBrand?,
        isCardTypeReliable: Boolean
    ) {
        val installmentOptions = InstallmentUtils.makeInstallmentOptions(params, cardBrand, isCardTypeReliable)
        assertTrue(installmentOptions.isEmpty())
    }

    @Test
    fun `make installment options returns installment models, when there are valid default installment options`() {
        val installmentOptionValues = listOf(1, 3, 5, 10)
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = installmentOptionValues,
                includeRevolving = false,
            ),
            shopperLocale = Locale.US,
        )

        val installmentOptions = InstallmentUtils.makeInstallmentOptions(installmentParams, null, false)

        val regularInstallmentOptions = installmentOptions.filter { model -> model.option == InstallmentOption.REGULAR }
        installmentOptionValues.forEachIndexed { index, optionValue ->
            assertEquals(optionValue, regularInstallmentOptions[index].numberOfInstallments)
        }
    }

    @Test
    fun `make installment options returns installment models, when there are valid card installment options`() {
        val installmentOptionValues = listOf(1, 3, 5, 10)
        val cardBrand = CardBrand(CardType.MASTERCARD)
        val installmentParams = InstallmentParams(
            cardBasedOptions = listOf(
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = installmentOptionValues,
                    cardBrand = cardBrand,
                    includeRevolving = false,
                ),
                InstallmentOptionParams.CardBasedInstallmentOptions(
                    values = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
                    cardBrand = cardBrand,
                    includeRevolving = false,
                ),
            ),
            shopperLocale = Locale.US,
        )

        val installmentOptions = InstallmentUtils.makeInstallmentOptions(
            installmentParams = installmentParams,
            cardBrand = cardBrand,
            isCardTypeReliable = true,
        )

        val regularInstallmentOptions = installmentOptions.filter { model -> model.option == InstallmentOption.REGULAR }
        installmentOptionValues.forEachIndexed { index, optionValue ->
            assertEquals(optionValue, regularInstallmentOptions[index].numberOfInstallments)
        }
    }

    @Test
    fun `make installment options returns one time installment model, when there are valid installment options`() {
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(1, 3, 5, 10),
                includeRevolving = false,
            ),
            shopperLocale = Locale.US,
        )

        val installmentOptions = InstallmentUtils.makeInstallmentOptions(installmentParams, null, false)

        val oneTimeInstallmentOptions =
            installmentOptions.filter { model -> model.option == InstallmentOption.ONE_TIME }
        assertEquals(1, oneTimeInstallmentOptions.size)
    }

    @Test
    fun `make installment options returns revolving installment model, when is revolving`() {
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(1, 3, 5, 10),
                includeRevolving = true,
            ),
            shopperLocale = Locale.US,
        )

        val installmentOptions = InstallmentUtils.makeInstallmentOptions(installmentParams, null, false)

        val revolvingInstallmentOptions =
            installmentOptions.filter { model -> model.option == InstallmentOption.REVOLVING }
        assertEquals(1, revolvingInstallmentOptions.size)
    }

    @Test
    fun `make installment options does not return revolving installment model, when is not revolving`() {
        val installmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(1, 3, 5, 10),
                includeRevolving = false,
            ),
            shopperLocale = Locale.US,
        )

        val installmentOptions = InstallmentUtils.makeInstallmentOptions(installmentParams, null, false)

        val revolvingInstallmentOptions =
            installmentOptions.filter { model -> model.option == InstallmentOption.REVOLVING }
        assertTrue(revolvingInstallmentOptions.isEmpty())
    }

    @Test
    fun `get text for installment option provides empty text, if installment option is null`() {
        val installmentOptionText = InstallmentUtils.getTextForInstallmentOption(mock(), null)
        assertTrue(installmentOptionText.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("noStringArgumentInstallmentSourceForGetTextForInstallmentOption")
    fun `get text for installment option gets a string, if installment option is one time`(
        installmentModel: InstallmentModel,
        @StringRes textResourceId: Int
    ) {
        InstallmentUtils.getTextForInstallmentOption(context, installmentModel)

        verify(context).getString(textResourceId)
    }

    @ParameterizedTest
    @MethodSource("numberOfInstallmentsStringSourceForGetTextForInstallmentOption")
    fun `get text for installment option gets a string, if installment option is regular and amount is not shown`(
        installmentModel: InstallmentModel
    ) {
        val textResourceId = R.string.checkout_card_installments_option_regular
        val formattedNumberOfInstallments =
            installmentModel.numberOfInstallments?.formatToLocalizedString(installmentModel.shopperLocale)

        InstallmentUtils.getTextForInstallmentOption(context, installmentModel)

        verify(context).getString(textResourceId, formattedNumberOfInstallments)
    }

    @ParameterizedTest
    @MethodSource("amountShownStringSourceForGetTextForInstallmentOption")
    fun `get text for installment option gets a string, if installment option is regular and amount is shown`(
        installmentModel: InstallmentModel,
        installmentAmount: String
    ) {
        val textResourceId = R.string.checkout_card_installments_option_regular_with_price
        val formattedNumberOfInstallments =
            installmentModel.numberOfInstallments?.formatToLocalizedString(installmentModel.shopperLocale)

        InstallmentUtils.getTextForInstallmentOption(context, installmentModel)

        verify(context).getString(textResourceId, formattedNumberOfInstallments, installmentAmount)
    }

    @ParameterizedTest
    @MethodSource("noValidInstallmentOptionForMakeInstallmentModelObject")
    fun `make installment model object returns null, if installment option does not have valid type`(
        installmentModel: InstallmentModel?
    ) {
        assertNull(InstallmentUtils.makeInstallmentModelObject(installmentModel))
    }

    @ParameterizedTest
    @MethodSource("validInstallmentOptionForMakeInstallmentModelObject")
    fun `make installment model object returns installments object, if installment option is regular`(
        installmentModel: InstallmentModel
    ) {
        val installments = InstallmentUtils.makeInstallmentModelObject(installmentModel)

        assertEquals(installmentModel.option.type, installments?.plan)
        assertEquals(installmentModel.numberOfInstallments, installments?.value)
    }

    @ParameterizedTest
    @MethodSource("validInstallmentOptionsForIsCardBasedOptionsValid")
    fun `is card based options valid returns true`(
        installmentOptions: List<InstallmentOptions.CardBasedInstallmentOptions>?
    ) {
        assertTrue(InstallmentUtils.isCardBasedOptionsValid(installmentOptions))
    }

    @ParameterizedTest
    @MethodSource("invalidInstallmentOptionsForIsCardBasedOptionsValid")
    fun `is card based options valid returns false`(
        installmentOptions: List<InstallmentOptions.CardBasedInstallmentOptions>?
    ) {
        assertFalse(InstallmentUtils.isCardBasedOptionsValid(installmentOptions))
    }

    @ParameterizedTest
    @MethodSource("validInstallmentConfigurationForAreInstallmentValuesValid")
    fun `are installment values valid returns true`(
        installmentConfiguration: InstallmentConfiguration
    ) {
        assertTrue(InstallmentUtils.areInstallmentValuesValid(installmentConfiguration))
    }

    @ParameterizedTest
    @MethodSource("invalidInstallmentConfigurationForAreInstallmentValuesValid")
    fun `are installment values valid returns false`(
        installmentConfiguration: InstallmentConfiguration
    ) {
        assertFalse(InstallmentUtils.areInstallmentValuesValid(installmentConfiguration))
    }

    companion object {
        @JvmStatic
        fun noValidInstallmentsSourceForMakeInstallmentOptions() = listOf(
            arguments(InstallmentParams(shopperLocale = Locale.US), CardBrand(CardType.MASTERCARD), true),
            arguments(
                InstallmentParams(
                    defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                        values = listOf(),
                        includeRevolving = true,
                    ),
                    amount = Amount("EUR", 100L),
                    shopperLocale = Locale.US,
                    showInstallmentAmount = true,
                ),
                CardBrand(CardType.VISA),
                true,
            ),
            arguments(
                InstallmentParams(
                    defaultOptions = null,
                    cardBasedOptions = listOf(),
                    amount = Amount("EUR", 100L),
                    shopperLocale = Locale.US,
                    showInstallmentAmount = true,
                ),
                CardBrand(CardType.VISA),
                true,
            ),
            arguments(
                InstallmentParams(
                    cardBasedOptions = listOf(
                        InstallmentOptionParams.CardBasedInstallmentOptions(
                            values = listOf(),
                            includeRevolving = false,
                            cardBrand = CardBrand(CardType.MASTERCARD),
                        ),
                    ),
                    shopperLocale = Locale.US,
                ),
                CardBrand(CardType.VISA),
                true,
            ),
            arguments(
                InstallmentParams(
                    cardBasedOptions = listOf(
                        InstallmentOptionParams.CardBasedInstallmentOptions(
                            values = listOf(),
                            includeRevolving = false,
                            cardBrand = CardBrand(CardType.MASTERCARD),
                        ),
                    ),
                    shopperLocale = Locale.US,
                ),
                CardBrand(CardType.MASTERCARD),
                false,
            ),
            arguments(null, null, false),
        )

        @JvmStatic
        fun noStringArgumentInstallmentSourceForGetTextForInstallmentOption() = listOf(
            arguments(
                InstallmentModel(
                    numberOfInstallments = null,
                    option = InstallmentOption.ONE_TIME,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
                R.string.checkout_card_installments_option_one_time,
            ),
            arguments(
                InstallmentModel(
                    numberOfInstallments = null,
                    option = InstallmentOption.REVOLVING,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
                R.string.checkout_card_installments_option_revolving,
            ),
        )

        @JvmStatic
        fun numberOfInstallmentsStringSourceForGetTextForInstallmentOption() = listOf(
            arguments(
                InstallmentModel(
                    numberOfInstallments = 2,
                    option = InstallmentOption.REGULAR,
                    amount = Amount("USD", 100L),
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
            ),
            arguments(
                InstallmentModel(
                    numberOfInstallments = 2,
                    option = InstallmentOption.REGULAR,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
            ),
        )

        @JvmStatic
        fun amountShownStringSourceForGetTextForInstallmentOption() = listOf(
            arguments(
                InstallmentModel(
                    numberOfInstallments = 2,
                    option = InstallmentOption.REGULAR,
                    amount = Amount("USD", 10000L),
                    shopperLocale = Locale.US,
                    showAmount = true,
                ),
                "$50.00",
            ),
            arguments(
                InstallmentModel(
                    numberOfInstallments = 3,
                    option = InstallmentOption.REGULAR,
                    amount = Amount("USD", 10000L),
                    shopperLocale = Locale.US,
                    showAmount = true,
                ),
                "$33.33",
            ),
            arguments(
                InstallmentModel(
                    numberOfInstallments = 4,
                    option = InstallmentOption.REGULAR,
                    amount = Amount("USD", 10000L),
                    shopperLocale = Locale.US,
                    showAmount = true,
                ),
                "$25.00",
            ),
        )

        @JvmStatic
        fun noValidInstallmentOptionForMakeInstallmentModelObject() = listOf(
            arguments(null),
            arguments(
                InstallmentModel(
                    numberOfInstallments = null,
                    option = InstallmentOption.ONE_TIME,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
            ),
        )

        @JvmStatic
        fun validInstallmentOptionForMakeInstallmentModelObject() = listOf(
            arguments(
                InstallmentModel(
                    numberOfInstallments = null,
                    option = InstallmentOption.REGULAR,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
            ),
            arguments(
                InstallmentModel(
                    numberOfInstallments = null,
                    option = InstallmentOption.REVOLVING,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                ),
            ),
        )

        @JvmStatic
        fun validInstallmentOptionsForIsCardBasedOptionsValid() = listOf(
            arguments(null),
            arguments(
                listOf(
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                ),
            ),
            arguments(
                listOf(
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.VISA)),
                ),
            ),
            arguments(
                listOf(
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.VISA)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.AMERICAN_EXPRESS)),
                ),
            ),
        )

        @JvmStatic
        fun invalidInstallmentOptionsForIsCardBasedOptionsValid() = listOf(
            arguments(
                listOf(
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                ),
            ),
            arguments(
                listOf(
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.VISA)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.MASTERCARD)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.VISA)),
                    InstallmentOptions.CardBasedInstallmentOptions(10, false, CardBrand(CardType.AMERICAN_EXPRESS)),
                ),
            ),
        )

        @JvmStatic
        fun validInstallmentConfigurationForAreInstallmentValuesValid() = listOf(
            arguments(InstallmentConfiguration()),
            arguments(
                InstallmentConfiguration(
                    defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
                        values = listOf(2, 6, 10),
                        includeRevolving = false,
                    ),
                ),
            ),
            arguments(
                InstallmentConfiguration(
                    cardBasedOptions = listOf(
                        InstallmentOptions.CardBasedInstallmentOptions(
                            values = listOf(2, 6, 10),
                            includeRevolving = false,
                            cardBrand = CardBrand(CardType.MASTERCARD),
                        ),
                        InstallmentOptions.CardBasedInstallmentOptions(
                            values = listOf(2, 6),
                            includeRevolving = false,
                            cardBrand = CardBrand(CardType.VISA),
                        ),
                    ),
                ),
            ),
            arguments(
                InstallmentConfiguration(
                    defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
                        values = listOf(2, 6),
                        includeRevolving = false,
                    ),
                    cardBasedOptions = listOf(
                        InstallmentOptions.CardBasedInstallmentOptions(
                            values = listOf(2, 6),
                            includeRevolving = false,
                            cardBrand = CardBrand(CardType.MASTERCARD),
                        ),
                    ),
                ),
            ),
        )

        @JvmStatic
        fun invalidInstallmentConfigurationForAreInstallmentValuesValid() = listOf(
            arguments(
                mock<InstallmentConfiguration>().apply {
                    whenever(defaultOptions).thenReturn(
                        InstallmentOptions.DefaultInstallmentOptions(
                            values = listOf(0),
                            includeRevolving = false,
                        ),
                    )
                },
            ),
            arguments(
                mock<InstallmentConfiguration>().apply {
                    whenever(defaultOptions).thenReturn(
                        InstallmentOptions.DefaultInstallmentOptions(
                            values = listOf(1),
                            includeRevolving = false,
                        ),
                    )
                },
            ),
            arguments(
                mock<InstallmentConfiguration>().apply {
                    whenever(cardBasedOptions).thenReturn(
                        listOf(
                            InstallmentOptions.CardBasedInstallmentOptions(
                                values = listOf(1),
                                includeRevolving = false,
                                cardBrand = CardBrand(CardType.MASTERCARD),
                            ),
                        ),
                    )
                },
            ),
            arguments(
                mock<InstallmentConfiguration>().apply {
                    whenever(defaultOptions).thenReturn(
                        InstallmentOptions.DefaultInstallmentOptions(
                            values = listOf(3, 4),
                            includeRevolving = false,
                        ),
                    )
                    whenever(cardBasedOptions).thenReturn(
                        listOf(
                            InstallmentOptions.CardBasedInstallmentOptions(
                                values = listOf(2, 3, 1),
                                includeRevolving = false,
                                cardBrand = CardBrand(CardType.MASTERCARD),
                            ),
                        ),
                    )
                },
            ),
        )
    }
}
