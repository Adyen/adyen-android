package com.adyen.checkout.util.internal;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/08/2017.
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Subclasses may override.
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Subclasses may override.
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Subclasses may override.
    }
}
