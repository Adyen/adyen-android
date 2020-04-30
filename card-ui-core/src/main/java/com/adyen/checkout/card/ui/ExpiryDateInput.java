/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 19/7/2019.
 */

package com.adyen.checkout.card.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;

import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.core.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ExpiryDateInput extends AdyenTextInputEditText {
    private static final String TAG = LogUtil.getTag();

    public static final String SEPARATOR = "/";
    private static final String DATE_FORMAT = "MM" + SEPARATOR + "yy";

    private static final int MAX_LENGTH = 5;
    private static final int MAX_SECOND_DIGIT_MONTH = 1;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ROOT);

    public ExpiryDateInput(@NonNull Context context) {
        this(context, null);
    }

    public ExpiryDateInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpiryDateInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        enforceMaxInputLength(MAX_LENGTH);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
        final String initial = editable.toString();
        // remove digits
        String processed = initial.replaceAll("\\D", "");
        // add separator
        processed = processed.replaceAll("(\\d{2})(?=\\d)", "$1" + SEPARATOR);
        // add tailing zero to month
        if (processed.length() == 1 && isStringInt(processed) && Integer.parseInt(processed) > MAX_SECOND_DIGIT_MONTH) {
            processed = "0" + processed;
        }

        if (!initial.equals(processed)) {
            editable.replace(0, initial.length(), processed);
        }

        super.afterTextChanged(editable);
    }

    /**
     * Get the {@link ExpiryDate} currenlty input by the user.
     *
     * @return The current entered Date or {@link ExpiryDate#EMPTY_DATE} if not valid.
     */
    @NonNull
    public ExpiryDate getDate() {
        final String normalizedExpiryDate = StringUtil.normalize(getRawValue());
        Logger.v(TAG, "getDate - " + normalizedExpiryDate);
        try {
            final Date parsedDate = mDateFormat.parse(normalizedExpiryDate);
            final Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(parsedDate);
            fixCalendarYear(calendar);
            // GregorianCalendar is 0 based
            return new ExpiryDate(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        } catch (ParseException e) {
            Logger.d(TAG, "getDate - value does not match expected pattern. " + e.getLocalizedMessage());
            return ExpiryDate.EMPTY_DATE;
        }
    }

    private void fixCalendarYear(@NonNull Calendar calendar) {
        // On SimpleDateFormat, if the truncated (yy) year is more than 20 years in the future it will use the previous century.
        // This is a small fix to correct for that without implementing or overriding the DateFormat class.
        final int yearsInCentury = 100;
        final Calendar currentCalendar = GregorianCalendar.getInstance();
        final int currentCentury = currentCalendar.get(Calendar.YEAR) / yearsInCentury;
        final int calendarCentury = calendar.get(Calendar.YEAR) / yearsInCentury;
        if (calendarCentury < currentCentury) {
            calendar.add(Calendar.YEAR, yearsInCentury);
        }
    }

    /**
     * Set an {@link ExpiryDate} to be displayed on the field.
     *
     * @param expiryDate The new value.
     */
    public void setDate(@Nullable ExpiryDate expiryDate) {
        if (expiryDate != null && expiryDate != ExpiryDate.EMPTY_DATE) {
            Logger.v(TAG, "setDate - " + expiryDate.getExpiryYear() + " " + expiryDate.getExpiryMonth());
            final Calendar calendar = GregorianCalendar.getInstance();
            calendar.clear();
            // first day of month, GregorianCalendar month is 0 based.
            calendar.set(expiryDate.getExpiryYear(), expiryDate.getExpiryMonth() - 1, 1);
            setText(mDateFormat.format(calendar.getTime()));
        } else {
            setText("");
        }
    }

    private boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
