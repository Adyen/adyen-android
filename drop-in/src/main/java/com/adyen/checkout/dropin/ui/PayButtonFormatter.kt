package com.adyen.checkout.dropin.ui

import android.content.Context
import androidx.annotation.StringRes
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.dropin.R
import java.util.Locale

internal object PayButtonFormatter {

    @Suppress("LongParameterList")
    fun getPayButtonText(
        amount: Amount,
        locale: Locale,
        localizedContext: Context,
        @StringRes emptyAmountStringResId: Int = R.string.pay_button,
        @StringRes zeroAmountStringResId: Int = R.string.confirm_preauthorization,
        @StringRes positiveAmountStringResId: Int = R.string.pay_button_with_value,
    ): String {
        return when {
            amount.isEmpty -> {
                localizedContext.getString(emptyAmountStringResId)
            }
            amount.isZero -> {
                localizedContext.getString(zeroAmountStringResId)
            }
            else -> {
                localizedContext.getString(
                    positiveAmountStringResId,
                    CurrencyUtils.formatAmount(amount, locale)
                )
            }
        }
    }
}
