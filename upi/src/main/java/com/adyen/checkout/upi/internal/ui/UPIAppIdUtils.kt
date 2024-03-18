/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.AppId
import com.adyen.checkout.core.Environment
import com.adyen.checkout.upi.internal.ui.model.UPIApp

// TODO: Add tests
internal fun List<AppId>.mapToUPIApp(environment: Environment): List<UPIApp> = mapNotNull { (id, name) ->
    if (id != null && name != null) {
        UPIApp(id, name, environment)
    } else {
        null
    }
}
