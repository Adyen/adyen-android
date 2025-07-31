/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/7/2025.
 */

package com.adyen.checkout.core.common.internal.model

import android.os.Parcel
import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parceler
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object JSONObjectParceler : Parceler<JSONObject?> {

    private const val FLAG_NULL = 0
    private const val FLAG_NON_NULL = 1

    override fun create(parcel: Parcel): JSONObject? {
        return when (parcel.readInt()) {
            FLAG_NULL -> null
            FLAG_NON_NULL -> JSONObject(parcel.readString() ?: "{}")
            else -> throw IllegalArgumentException("Invalid flag.")
        }
    }

    override fun JSONObject?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeInt(FLAG_NULL)
        } else {
            parcel.writeInt(FLAG_NON_NULL)
            parcel.writeString(this.toString())
        }
    }
}
