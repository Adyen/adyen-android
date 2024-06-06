/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.internal.ui.model

import android.os.Parcelable
import com.adyen.checkout.components.core.Amount
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class GiftCardPaymentConfirmationData(
    val amountPaid: Amount,
    val remainingBalance: Amount,
    val shopperLocale: Locale,
    val brand: String,
    val lastFourDigits: String,
    val paymentMethodName: String,
) : Parcelable
