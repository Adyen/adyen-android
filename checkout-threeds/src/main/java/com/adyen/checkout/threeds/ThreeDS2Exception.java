/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 21/11/2018.
 */

package com.adyen.checkout.threeds;

import android.support.annotation.NonNull;

public final class ThreeDS2Exception extends Exception {

    private ThreeDS2Exception(@NonNull String message) {
        super(message);
    }

    private ThreeDS2Exception(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    @NonNull
    public static ThreeDS2Exception from(@NonNull String message) {
        return new ThreeDS2Exception(message);
    }

    @NonNull
    public static ThreeDS2Exception from(@NonNull String message, @NonNull Throwable cause) {
        return new ThreeDS2Exception(message, cause);
    }
}
