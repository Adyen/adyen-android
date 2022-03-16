/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.util

import android.os.Parcel

object ParcelUtils {
    private const val BOOLEAN_TRUE_VALUE = 1
    private const val BOOLEAN_FALSE_VALUE = 0

    /**
     * Write boolean in to Parcel.
     */
    @JvmStatic
    fun writeBoolean(dest: Parcel, value: Boolean) {
        dest.writeInt(if (value) BOOLEAN_TRUE_VALUE else BOOLEAN_FALSE_VALUE)
    }

    /**
     * Read boolean from Parcel.
     */
    @JvmStatic
    fun readBoolean(input: Parcel): Boolean {
        return input.readInt() == BOOLEAN_TRUE_VALUE
    }
}
