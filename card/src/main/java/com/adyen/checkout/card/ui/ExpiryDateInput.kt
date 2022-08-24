/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/3/2022.
 */
package com.adyen.checkout.card.ui

import android.content.Context
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.d
import com.adyen.checkout.core.log.Logger.v
import com.adyen.checkout.core.util.StringUtil.normalize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class ExpiryDateInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenTextInputEditText(context, attrs, defStyleAttr) {

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

    init {
        enforceMaxInputLength(MAX_LENGTH)
        // Make sure DateFormat only accepts the correct formatting.
        dateFormat.isLenient = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE)
        }
    }

    public override fun afterTextChanged(editable: Editable) {
        val initial = editable.toString()
        // remove digits
        var processed = initial.replace("\\D".toRegex(), "")
        // add separator
        processed = processed.replace("(\\d{2})(?=\\d)".toRegex(), "$1$SEPARATOR")
        // add tailing zero to month
        if (processed.length == 1 && isStringInt(processed) && processed.toInt() > MAX_SECOND_DIGIT_MONTH) {
            processed = "0$processed"
        }
        if (initial != processed) {
            editable.replace(0, initial.length, processed)
        }
        super.afterTextChanged(editable)
    }

    // GregorianCalendar is 0 based
    // first day of month, GregorianCalendar month is 0 based.
    /**
     * Set an [ExpiryDate] to be displayed on the field.
     *
     * @param expiryDate The new value.
     */
    /**
     * Get the [ExpiryDate] currenlty input by the user.
     *
     * @return The current entered Date or [ExpiryDate.EMPTY_DATE] if not valid.
     */
    var date: ExpiryDate
        get() {
            val normalizedExpiryDate = normalize(rawValue)
            v(TAG, "getDate - $normalizedExpiryDate")
            return try {
                val parsedDate = dateFormat.parse(normalizedExpiryDate)
                val calendar = GregorianCalendar.getInstance()
                calendar.time = parsedDate
                fixCalendarYear(calendar)
                // GregorianCalendar is 0 based
                ExpiryDate(calendar[Calendar.MONTH] + 1, calendar[Calendar.YEAR])
            } catch (e: ParseException) {
                d(TAG, "getDate - value does not match expected pattern. " + e.localizedMessage)
                if (rawValue.isEmpty()) ExpiryDate.EMPTY_DATE else ExpiryDate.INVALID_DATE
            }
        }
        set(expiryDate) {
            if (expiryDate !== ExpiryDate.EMPTY_DATE) {
                v(TAG, "setDate - " + expiryDate.expiryYear + " " + expiryDate.expiryMonth)
                val calendar = GregorianCalendar.getInstance()
                calendar.clear()
                // first day of month, GregorianCalendar month is 0 based.
                calendar[expiryDate.expiryYear, expiryDate.expiryMonth - 1] = 1
                setText(dateFormat.format(calendar.time))
            } else {
                setText("")
            }
        }

    private fun fixCalendarYear(calendar: Calendar) {
        // On SimpleDateFormat, if the truncated (yy) year is more than 20 years in the future it will use the previous century.
        // This is a small fix to correct for that without implementing or overriding the DateFormat class.
        @Suppress("MagicNumber")
        val yearsInCentury = 100
        val currentCalendar = GregorianCalendar.getInstance()
        val currentCentury = currentCalendar[Calendar.YEAR] / yearsInCentury
        val calendarCentury = calendar[Calendar.YEAR] / yearsInCentury
        if (calendarCentury < currentCentury) {
            calendar.add(Calendar.YEAR, yearsInCentury)
        }
    }

    private fun isStringInt(s: String): Boolean {
        return try {
            s.toInt()
            true
        } catch (ex: NumberFormatException) {
            false
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        const val SEPARATOR = "/"
        private const val DATE_FORMAT = "MM" + SEPARATOR + "yy"
        private const val MAX_LENGTH = 5
        private const val MAX_SECOND_DIGIT_MONTH = 1
    }
}
