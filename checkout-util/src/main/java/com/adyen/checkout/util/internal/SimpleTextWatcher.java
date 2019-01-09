/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/08/2017.
 */

package com.adyen.checkout.util.internal;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {
        // Subclasses may override.
    }

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        // Subclasses may override.
    }

    @Override
    public void afterTextChanged(@NonNull Editable s) {
        // Subclasses may override.
    }
}
