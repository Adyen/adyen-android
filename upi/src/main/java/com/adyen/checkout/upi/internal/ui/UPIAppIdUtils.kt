/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.App
import com.adyen.checkout.core.Environment
import com.adyen.checkout.upi.internal.ui.model.UPICollectItem

// TODO: Add tests
internal fun List<App>.mapToPaymentApp(environment: Environment): List<UPICollectItem.PaymentApp> =
    mapNotNull { (id, name) ->
        if (id != null && name != null) {
            UPICollectItem.PaymentApp(id, name, environment)
        } else {
            null
        }
    }
