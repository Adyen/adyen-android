/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/11/2019.
 */

package com.adyen.checkout.afterpay.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;

import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("SyntheticAccessor")
public class DateOfBirthInput extends AdyenTextInputEditText implements
        DatePickerDialog.OnDateSetListener,
        DialogInterface.OnShowListener,
        DialogInterface.OnCancelListener {

    public static final String SEPARATOR = "/";

    private Calendar mCalendar = Calendar.getInstance();
    private final DatePickerDialog mDatePickerDialog;

    public DateOfBirthInput(@NonNull Context context) {
        this(context, null);
    }

    public DateOfBirthInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor of DateOfBirthInput.
     */
    public DateOfBirthInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInputType(InputType.TYPE_NULL);

        mDatePickerDialog = new DatePickerDialog(
                getContext(),
                this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );

        mDatePickerDialog.setOnShowListener(this);
        mDatePickerDialog.setOnCancelListener(this);
        mDatePickerDialog.setCancelable(false);

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePicker();
                }
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    /**
     * Set date of birth.
     */
    public void setDate(@NonNull Calendar date) {
        mCalendar = date;
        setText(getFormatDate(date));
        mDatePickerDialog.updateDate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
    }

    @NonNull
    public Calendar getCalendar() {
        return mCalendar;
    }

    @Override
    public void onDateSet(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        setText(getFormatDate(mCalendar));

        focusOnNext();
    }

    @Override
    public void onShow(@NonNull DialogInterface dialog) {
        final ArrayList<View> touchableViews = mDatePickerDialog.getDatePicker().getTouchables();
        // show year picker first!
        if (!touchableViews.isEmpty()) {
            mDatePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        focusOnNext();
    }

    private void showDatePicker() {
        mDatePickerDialog.show();
    }

    private String getFormatDate(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH)
                + SEPARATOR + (calendar.get(Calendar.MONTH) + 1)
                + SEPARATOR + calendar.get(Calendar.YEAR);
    }


    private void focusOnNext() {
        final View nextView = focusSearch(FOCUS_DOWN);
        nextView.requestFocus();

        nextView.post(new Runnable() {
            @Override
            public void run() {
                final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(nextView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

    }
}
