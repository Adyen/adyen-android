/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class VoucherUIEvent {
    data object Success : VoucherUIEvent()
    data object PermissionDenied : VoucherUIEvent()
    data class Failure(val throwable: Throwable) : VoucherUIEvent()
}
