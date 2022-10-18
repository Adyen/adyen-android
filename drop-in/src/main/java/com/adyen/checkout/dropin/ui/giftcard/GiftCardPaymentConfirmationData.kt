/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.ui.giftcard

import android.os.Parcelable
import com.adyen.checkout.components.model.payments.Amount
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class GiftCardPaymentConfirmationData(
    val amountPaid: Amount,
    val remainingBalance: Amount,
    val shopperLocale: Locale,
    val brand: String,
    val lastFourDigits: String
) : Parcelable
