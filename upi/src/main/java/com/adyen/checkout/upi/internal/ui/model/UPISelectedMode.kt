/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.model

internal enum class UPISelectedMode {
    COLLECT,
    VPA,
    QR
}

// TODO: Probably instead of this there is a better way to handle mode toggle in UPIView?
internal fun UPIMode.mapToSelectedMode() = when (this) {
    is UPIMode.Collect -> UPISelectedMode.COLLECT
    UPIMode.Vpa -> UPISelectedMode.VPA
    UPIMode.Qr -> UPISelectedMode.QR
}
