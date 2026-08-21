/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/8/2026.
 */

package com.adyen.checkout.ui.internal.theme

import android.os.Parcel
import com.adyen.checkout.ui.theme.CheckoutColor
import kotlinx.parcelize.Parceler

// Parcelize does not support value classes, so we need to parcelize the underlying value manually.
internal object CheckoutColorParceler : Parceler<CheckoutColor> {

    override fun create(parcel: Parcel) = CheckoutColor(parcel.readLong())

    override fun CheckoutColor.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(value)
    }
}
