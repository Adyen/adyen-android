/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 26/8/2019.
 */

package com.adyen.checkout.core.util;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

public final class ParcelUtils {

    public static final int NO_FILE_DESCRIPTOR = 0;

    private static final int BOOLEAN_TRUE_VALUE = 1;
    private static final int BOOLEAN_FALSE_VALUE = 0;

    private ParcelUtils() {
        throw new NoConstructorException();
    }

    /**
     * Write boolean in to Parcel.
     */
    public static void writeBoolean(@NonNull Parcel dest, boolean value) {
        dest.writeInt(value ? BOOLEAN_TRUE_VALUE : BOOLEAN_FALSE_VALUE);
    }

    /**
     * Read boolean from Parcel.
     */
    public static boolean readBoolean(@NonNull Parcel in) {
        return in.readInt() == BOOLEAN_TRUE_VALUE;
    }
}
