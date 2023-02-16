/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/1/2023.
 */

package com.adyen.checkout.qrcode.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class QrCodeUIEvent {
    sealed class QrImageDownloadResult : QrCodeUIEvent() {
        object Success : QrImageDownloadResult()
        data class Failure(val throwable: Throwable) : QrImageDownloadResult()
    }
}
