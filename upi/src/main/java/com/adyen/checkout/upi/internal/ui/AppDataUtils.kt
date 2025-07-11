/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.AppData
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal fun List<AppData>.mapToPaymentApp(
    environment: Environment,
    selectedAppId: String?
): List<UPIIntentItem.PaymentApp> = mapNotNull { (id, name) ->
    if (!id.isNullOrEmpty() && !name.isNullOrEmpty()) {
        val isSelected = selectedAppId == id
        UPIIntentItem.PaymentApp(id, name, environment, isSelected)
    } else {
        null
    }
}
