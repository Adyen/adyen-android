/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old

import android.os.Parcelable
import com.adyen.checkout.card.old.internal.util.InstallmentUtils
import com.adyen.checkout.core.old.exception.CheckoutException
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for Installments in Card Component. This class can be used
 * to define installment options for all cards or specific [CardBrand]. [defaultOptions]
 * and [cardBasedOptions] can be combined together. In that case [InstallmentOptions] from
 * [cardBasedOptions] will override the option defined in [defaultOptions].
 *
 * Note: [cardBasedOptions] should contain only one [InstallmentOptions.CardBasedInstallmentOptions]
 * instance for a [CardBrand].
 *
 * @param defaultOptions Installment Options to be used for all card types.
 * @param cardBasedOptions Installment Options to be used for specific card types.
 * @param showInstallmentAmount A flag to show the installment amount.
 */
@Parcelize
data class InstallmentConfiguration(
    val defaultOptions: InstallmentOptions.DefaultInstallmentOptions? = null,
    val cardBasedOptions: List<InstallmentOptions.CardBasedInstallmentOptions> = emptyList(),
    val showInstallmentAmount: Boolean = false
) : Parcelable {

    init {
        if (!InstallmentUtils.isCardBasedOptionsValid(cardBasedOptions)) {
            throw CheckoutException("Installment Configuration has multiple rules for same card type.")
        }
        if (!InstallmentUtils.areInstallmentValuesValid(this)) {
            throw CheckoutException(
                "Installment Configuration contains invalid values for options. Values must be greater than 1.",
            )
        }
    }
}

/**
 * InstallmentOptions is used for defining the details of installment options.
 *
 * Note: All values specified in [values] must be greater than 1.
 */
sealed class InstallmentOptions : Parcelable {

    abstract val values: List<Int>
    abstract val includeRevolving: Boolean

    companion object {
        private const val STARTING_INSTALLMENT_VALUE = 2
    }

    /**
     * @param values see [InstallmentOptions.values]
     * @param includeRevolving see [InstallmentOptions.includeRevolving]
     * @param cardBrand a [CardBrand] to apply the given options
     */
    @Parcelize
    data class CardBasedInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean,
        val cardBrand: CardBrand
    ) : InstallmentOptions() {

        /**
         * @param maxInstallments Maximum number of installments
         *
         * Creates a [DefaultInstallmentOptions] instance with values in range [2, maxInstallments]
         */
        constructor(maxInstallments: Int, includeRevolving: Boolean, cardBrand: CardBrand) :
            this((STARTING_INSTALLMENT_VALUE..maxInstallments).toList(), includeRevolving, cardBrand)
    }

    /**
     * @param values see [InstallmentOptions.values]
     * @param includeRevolving see [InstallmentOptions.includeRevolving]
     */
    @Parcelize
    data class DefaultInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean
    ) : InstallmentOptions() {

        /**
         * @param maxInstallments Maximum number of installments
         *
         * Creates a [DefaultInstallmentOptions] instance with values in range [2, maxInstallments]
         */
        constructor(maxInstallments: Int, includeRevolving: Boolean) :
            this((STARTING_INSTALLMENT_VALUE..maxInstallments).toList(), includeRevolving)
    }
}
