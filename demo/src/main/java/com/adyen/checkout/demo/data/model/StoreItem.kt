/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/2/2024.
 */

package com.adyen.checkout.demo.data.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.demo.ui.formatAmount
import java.util.Locale

data class StoreItem(
    val title: String,
    val imageUrl: String,
    val price: Amount,
) {
    val priceText
        get() = price.formatAmount(Locale.US)
}
