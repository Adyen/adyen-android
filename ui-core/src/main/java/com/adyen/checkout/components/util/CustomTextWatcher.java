/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.components.util;

import android.text.Editable;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

public abstract class CustomTextWatcher extends SimpleTextWatcher {
    private boolean mChangedByUser;

    private boolean isChangedByUser() {
        return mChangedByUser;
    }

    @CallSuper
    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        mChangedByUser = Math.abs(count - before) == 1;
    }

    @Override
    public void afterTextChanged(@NonNull Editable s) {
        if (isChangedByUser()) {
            afterTextChangedByUser(s);
        }
    }

    public abstract void afterTextChangedByUser(@NonNull Editable s);
}
