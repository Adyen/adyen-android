/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/12/2022.
 */

package com.adyen.checkout.ui.core.old.internal.util

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.isZero
import com.adyen.checkout.ui.core.R
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PayButtonFormatter {

    @Suppress("LongParameterList")
    fun getPayButtonText(
        amount: Amount?,
        locale: Locale,
        localizedContext: Context,
        @StringRes emptyAmountStringResId: Int = R.string.pay_button,
        @StringRes zeroAmountStringResId: Int = R.string.confirm_preauthorization,
        @StringRes positiveAmountStringResId: Int = R.string.pay_button_with_value,
    ): String {
        return when {
            amount == null -> {
                localizedContext.getString(emptyAmountStringResId)
            }

            amount.isZero -> {
                localizedContext.getString(zeroAmountStringResId)
            }

            else -> {
                localizedContext.getString(positiveAmountStringResId, CurrencyUtils.formatAmount(amount, locale))
            }
        }
    }
}
