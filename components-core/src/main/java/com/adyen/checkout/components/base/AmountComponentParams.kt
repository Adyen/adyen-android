/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.payments.Amount

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AmountComponentParams {
    val amount: Amount
}
