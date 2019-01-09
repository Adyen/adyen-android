/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Spinner;

import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;

public class SimpleItemSpinnerValidator
        extends ValidationCheckDelegateBase {

    private InputDetail mInputDetail;
    private Spinner mSpinner;

    public SimpleItemSpinnerValidator(@NonNull InputDetail inputDetail, @NonNull Spinner spinner) {
        mInputDetail = inputDetail;
        mSpinner = spinner;
    }

    @Nullable
    private String getIdFromItemSpinner() {
        String id = null;

        if (mSpinner != null && mSpinner.getSelectedItem() != null) {
            Item item = (Item) mSpinner.getSelectedItem();
            if (!TextUtils.isEmpty(item.getId())) {
                id = item.getId();
            }
        }

        return id;
    }

    @NonNull
    @Override
    public ValidationState getValidationState() {
        if (mInputDetail.isOptional()) {
            return ValidationState.VALID;
        }

        if (getIdFromItemSpinner() == null) {
            return ValidationState.FIELD_EMPTY;
        } else {
            return ValidationState.VALID;
        }
    }
}
