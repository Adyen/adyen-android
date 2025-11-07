/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.old.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.old.Environment
import java.util.Locale

internal data class GiftCardPaymentMethodModel(
    val imageId: String,
    val lastFour: String,
    val amount: Amount?,
    val transactionLimit: Amount?,
    val shopperLocale: Locale?,
    // We need the environment to load the logo
    val environment: Environment,
) : PaymentMethodListItem {
    override fun getViewType(): Int = PaymentMethodListItem.GIFT_CARD_PAYMENT_METHOD
}
