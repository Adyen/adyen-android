/*
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.ui.internal.common.view.DatePickerWidget;

import java.util.Date;

public class DateValidator extends ValidationCheckDelegateBase implements DatePickerWidget.OnDateChangeListener {

    private DatePickerWidget mDatePicker;
    private InputDetail mInputDetail;

    public DateValidator(@NonNull InputDetail inputDetail, @NonNull DatePickerWidget datePickerWidget) {
        mInputDetail = inputDetail;
        mDatePicker = datePickerWidget;

        mDatePicker.addOnDateChangeListener(this);
    }

    @NonNull
    @Override
    public ValidationState getValidationState() {
        if (mInputDetail.isOptional()) {
            return ValidationState.VALID;
        }

        if (mDatePicker.getDate() == null) {
            return ValidationState.FIELD_EMPTY;
        } else {
            return ValidationState.VALID;
        }
    }

    @Override
    public void onDateChanged(@NonNull Date date) {
        isValid();
    }
}
