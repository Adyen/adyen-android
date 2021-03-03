/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.InputData;

public class BlikInputData implements InputData {

    private String mBlikCode = "";

    @NonNull
    public String getBlikCode() {
        return mBlikCode;
    }

    public void setBlikCode(@NonNull String blikCode) {
        mBlikCode = blikCode;
    }
}
