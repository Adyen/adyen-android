/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
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
