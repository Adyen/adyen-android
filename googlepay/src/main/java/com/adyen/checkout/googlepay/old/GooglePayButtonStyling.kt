/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/10/2024.
 */

package com.adyen.checkout.googlepay.old

import android.os.Parcelable
import androidx.annotation.Dimension
import com.google.android.gms.wallet.button.ButtonConstants
import kotlinx.parcelize.Parcelize

/**
 * Object to style the Google Pay button. Check [the Google docs](https://developers.google.com/pay/api/android/guides/resources/pay-button-api) for more details.
 *
 * @param buttonTheme Affects the color scheme of the button.
 * @param buttonType Changes the text displayed inside of the button.
 * @param cornerRadius Sets the corner radius of the button. For example, passing 16 means the radius will be 16 dp.
 */
@Suppress("MaxLineLength")
@Parcelize
data class GooglePayButtonStyling(
    val buttonTheme: GooglePayButtonTheme? = null,
    val buttonType: GooglePayButtonType? = null,
    @Dimension(Dimension.DP) val cornerRadius: Int? = null,
) : Parcelable

enum class GooglePayButtonTheme(
    val value: Int,
) {
    LIGHT(ButtonConstants.ButtonTheme.LIGHT),
    DARK(ButtonConstants.ButtonTheme.DARK),
}

enum class GooglePayButtonType(
    val value: Int,
) {
    BUY(ButtonConstants.ButtonType.BUY),
    BOOK(ButtonConstants.ButtonType.BOOK),
    CHECKOUT(ButtonConstants.ButtonType.CHECKOUT),
    DONATE(ButtonConstants.ButtonType.DONATE),
    ORDER(ButtonConstants.ButtonType.ORDER),
    PAY(ButtonConstants.ButtonType.PAY),
    SUBSCRIBE(ButtonConstants.ButtonType.SUBSCRIBE),
    PLAIN(ButtonConstants.ButtonType.PLAIN),
}
