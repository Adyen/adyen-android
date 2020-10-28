/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.InputData;

public class SepaInputData implements InputData {

    private String mName = "";
    private String mIban = "";

    @NonNull
    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        mName = name;
    }

    @NonNull
    public String getIban() {
        return mIban;
    }

    public void setIban(@NonNull String iban) {
        mIban = iban;
    }
}
