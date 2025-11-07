/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import androidx.annotation.StringRes
import com.adyen.checkout.dropin.old.internal.ui.model.GiftCardPaymentConfirmationData

internal sealed class GiftCardBalanceResult {
    class FullPayment(val data: GiftCardPaymentConfirmationData) : GiftCardBalanceResult()
    object RequestOrderCreation : GiftCardBalanceResult()
    object RequestPartialPayment : GiftCardBalanceResult()
    class Error(
        @StringRes val errorMessage: Int,
        val reason: String,
        val terminateDropIn: Boolean
    ) : GiftCardBalanceResult()
}
