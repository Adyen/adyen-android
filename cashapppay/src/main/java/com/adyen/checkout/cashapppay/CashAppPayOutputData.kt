/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/3/2023.
 */

package com.adyen.checkout.cashapppay

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.OutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CashAppPayOutputData(
    var isStorePaymentSelected: Boolean = false,
    val grantId: String? = null,
) : OutputData {
    override fun isValid(): Boolean {
        return grantId != null
    }
}
