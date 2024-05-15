/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/2/2023.
 */

package com.adyen.checkout.upi.internal.ui.model

internal sealed class UPIMode {
    data class Intent(val collectItems: List<UPIIntentItem>) : UPIMode()
    data object Vpa : UPIMode()
    data object Qr : UPIMode()
}
