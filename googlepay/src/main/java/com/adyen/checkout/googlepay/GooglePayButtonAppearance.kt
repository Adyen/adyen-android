/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/10/2024.
 */

package com.adyen.checkout.googlepay

import android.os.Parcelable
import androidx.annotation.Dimension
import com.google.android.gms.wallet.button.ButtonConstants
import kotlinx.parcelize.Parcelize

/**
 * Object to style the Google Pay button. Check [the Google docs](https://developers.google.com/pay/api/android/guides/resources/pay-button-api) for more details.
 *
 * @param theme Affects the color scheme of the button.
 * @param type Changes the text displayed inside the button.
 * @param cornerRadius Sets the corner radius of the button. For example, passing 16 means the radius will be 16 dp.
 */
@Suppress("MaxLineLength")
@Parcelize
data class GooglePayButtonAppearance(
    val theme: GooglePayButtonTheme? = null,
    val type: GooglePayButtonType? = null,
    @Dimension(Dimension.DP) val cornerRadius: Int? = null,
) : Parcelable

@Parcelize
@ConsistentCopyVisibility
data class GooglePayButtonTheme private constructor(
    val value: Int,
) : Parcelable {
    companion object {
        val LIGHT = GooglePayButtonTheme(ButtonConstants.ButtonTheme.LIGHT)
        val DARK = GooglePayButtonTheme(ButtonConstants.ButtonTheme.DARK)
    }
}

@Parcelize
@ConsistentCopyVisibility
data class GooglePayButtonType private constructor(
    val value: Int,
) : Parcelable {
    companion object {
        val BOOK = GooglePayButtonType(ButtonConstants.ButtonType.BOOK)
        val BUY = GooglePayButtonType(ButtonConstants.ButtonType.BUY)
        val CHECKOUT = GooglePayButtonType(ButtonConstants.ButtonType.CHECKOUT)
        val DONATE = GooglePayButtonType(ButtonConstants.ButtonType.DONATE)
        val ORDER = GooglePayButtonType(ButtonConstants.ButtonType.ORDER)
        val PAY = GooglePayButtonType(ButtonConstants.ButtonType.PAY)
        val PLAIN = GooglePayButtonType(ButtonConstants.ButtonType.PLAIN)
        val SUBSCRIBE = GooglePayButtonType(ButtonConstants.ButtonType.SUBSCRIBE)
        val PIX = GooglePayButtonType(ButtonConstants.ButtonType.PIX)
        val EWALLET = GooglePayButtonType(ButtonConstants.ButtonType.EWALLET)
    }
}
