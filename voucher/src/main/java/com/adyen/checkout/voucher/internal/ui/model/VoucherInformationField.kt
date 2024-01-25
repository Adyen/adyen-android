/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/12/2023.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class VoucherInformationField(
    @StringRes val labelResId: Int,
    val value: String
)
