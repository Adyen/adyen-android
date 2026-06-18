/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

internal enum class InstallmentPlan(val type: String?) {
    ONE_TIME(null),
    REGULAR("regular"),
    REVOLVING("revolving"),
}
