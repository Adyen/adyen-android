/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;

import com.adyen.checkout.ui.R;

class EditTextVisualizationControl implements VisibilityControlDelegate {

    private EditText mEditText;

    EditTextVisualizationControl(EditText editText) {
        mEditText = editText;
    }

    @Override
    public void setEditableInputView() {
        mEditText.getBackground().clearColorFilter();
        mEditText.setEnabled(true);
    }

    @Override
    public void setReadOnlyInputView() {
        mEditText.getBackground().clearColorFilter();
        mEditText.setEnabled(false);
    }

    @Override
    public void setErrorFeedbackView() {
        @ColorInt int color = ContextCompat.getColor(mEditText.getContext(), R.color.error);
        mEditText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
