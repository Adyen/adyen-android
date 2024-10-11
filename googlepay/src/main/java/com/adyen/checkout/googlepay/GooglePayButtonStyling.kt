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

@Parcelize
data class GooglePayButtonStyling(
    val buttonTheme: GooglePayButtonTheme?,
    val buttonType: GooglePayButtonType?,
    @Dimension(Dimension.DP) val cornerRadius: Int?,
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
