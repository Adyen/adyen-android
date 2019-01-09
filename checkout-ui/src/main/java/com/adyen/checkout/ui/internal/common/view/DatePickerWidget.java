/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/12/2018.
 */

package com.adyen.checkout.ui.internal.common.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class DatePickerWidget extends android.support.v7.widget.AppCompatTextView
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Date mDate;
    private DateFormat mDisplayDateFormat;
    private HashSet<OnDateChangeListener> mListener = new HashSet<>();

    public DatePickerWidget(@NonNull Context context) {
        this(context, null);
    }

    public DatePickerWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, android.support.v7.appcompat.R.attr.spinnerStyle);
        mDisplayDateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
    }

    public DatePickerWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        //Always overrides style to spinner
        this(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
    }

    @NonNull
    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        if (mDate != null) {
            savedState.mTime = mDate.getTime();
        }
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Parcelable state) {
        if (!(state instanceof DatePickerWidget.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        DatePickerWidget.SavedState savedState = (DatePickerWidget.SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.mTime != 0L) {
            mDate = new Date(savedState.mTime);
            setText(mDisplayDateFormat.format(mDate));
            notifyOnDateChangedListeners();
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        showPickerDialog();
    }

    private void showPickerDialog() {
        final Calendar c = Calendar.getInstance();

        if (mDate != null) {
            c.setTime(mDate);
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), this, year, month, day);
        dateDialog.show();
    }

    /**
     * Set a {@link Date} object to be displayed.
     * @param date The date to be set.
     */
    public void setDate(@NonNull Date date) {
        mDate = date;
        setText(mDisplayDateFormat.format(mDate));
        notifyOnDateChangedListeners();
    }

    @Override
    public void onDateSet(@NonNull DatePicker view, int year, int month, int dayOfMonth) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        mDate = cal.getTime();

        setText(mDisplayDateFormat.format(mDate));
        notifyOnDateChangedListeners();
    }

    @Nullable
    public Date getDate() {
        return mDate;
    }

    /**
     * Sets a new {@link DateFormat} to be used to display the Date.
     * @param dateFormat The new display format.
     */
    public void setDisplayDateFormat(@NonNull DateFormat dateFormat) {
        mDisplayDateFormat = dateFormat;
    }

    /**
     * Adds an {@link OnDateChangeListener} to be notified when the selected {@link Date} changes.
     * @param listener The listener to be added.
     */
    public void addOnDateChangeListener(@NonNull OnDateChangeListener listener) {
        mListener.add(listener);
    }

    /**
     * Removes an {@link OnDateChangeListener} to no longer be notified of date changes.
     * @param listener The listener to be removed.
     */
    public void removeOnDateChangeListener(@NonNull OnDateChangeListener listener) {
        mListener.remove(listener);
    }

    private void notifyOnDateChangedListeners() {
        for (OnDateChangeListener listener : mListener) {
            listener.onDateChanged(mDate);
        }
    }

    /**
     * User state that is stored by DatePickerWidget for implementing {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {
        @NonNull
        public static final Parcelable.Creator<DatePickerWidget.SavedState> CREATOR =
                new Parcelable.Creator<DatePickerWidget.SavedState>() {
                    public DatePickerWidget.SavedState createFromParcel(Parcel in) {
                        return new DatePickerWidget.SavedState(in);
                    }

                    public DatePickerWidget.SavedState[] newArray(int size) {
                        return new DatePickerWidget.SavedState[size];
                    }
                };

        long mTime = 0L;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mTime = in.readLong();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(mTime);
        }
    }

    /**
     * Listener to be notified when the specified {@link Date} changes.
     */
    public interface OnDateChangeListener {

        /**
         * Called when the Date has changed.
         * @param date New Date value.
         */
        void onDateChanged(@NonNull Date date);
    }

}
