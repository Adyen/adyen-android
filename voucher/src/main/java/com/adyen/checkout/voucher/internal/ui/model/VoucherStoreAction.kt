/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 28/12/2023.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class VoucherStoreAction {
    data class DownloadPdf(val downloadUrl: String) : VoucherStoreAction()
    object SaveAsImage: VoucherStoreAction()
}
