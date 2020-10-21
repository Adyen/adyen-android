/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/12/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;

public enum Gender {

    M("MALE"),
    F("FEMALE"),
    U("unknown");

    private final String mValue;

    Gender(@NonNull String value) {
        this.mValue = value;
    }

    @NonNull
    public String getValue() {
        return mValue;
    }
}
