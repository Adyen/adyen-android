/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 28/2/2023.
 */

package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep

@Keep
enum class InstallmentPlan(val plan: String) {
    REGULAR("regular"),
    REVOLVING("revolving")
}
