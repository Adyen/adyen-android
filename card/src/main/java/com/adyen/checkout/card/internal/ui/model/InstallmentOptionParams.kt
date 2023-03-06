/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 24/2/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import android.os.Parcelable
import com.adyen.checkout.card.CardBrand
import kotlinx.parcelize.Parcelize

/**
 * InstallmentOptionParams is used for defining the details of installment options.
 *
 * Note: All values specified in [values] must be greater than 1.
 */
internal sealed class InstallmentOptionParams : Parcelable {
    abstract val values: List<Int>
    abstract val includeRevolving: Boolean

    /**
     * @param values see [InstallmentOptionParams.values]
     * @param includeRevolving see [InstallmentOptionParams.includeRevolving]
     * @param cardBrand a [CardBrand] to apply the given options
     */
    @Parcelize
    data class CardBasedInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean,
        val cardBrand: CardBrand
    ) : InstallmentOptionParams()

    /**
     * @param values see [InstallmentOptionParams.values]
     * @param includeRevolving see [InstallmentOptionParams.includeRevolving]
     */
    @Parcelize
    data class DefaultInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean
    ) : InstallmentOptionParams()
}
