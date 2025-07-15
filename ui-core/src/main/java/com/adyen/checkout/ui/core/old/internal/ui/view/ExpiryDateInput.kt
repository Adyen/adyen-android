/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */
package com.adyen.checkout.ui.core.old.internal.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.old.internal.ui.model.INVALID_DATE
import com.adyen.checkout.core.old.internal.util.StringUtil.normalize
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.ui.model.ExpiryDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class ExpiryDateInput
@JvmOverloads
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

    init {
        enforceMaxInputLength(MAX_LENGTH)
        // Make sure DateFormat only accepts the correct formatting.
        dateFormat.isLenient = false
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
    var date: ExpiryDate
        get() {
            val normalizedExpiryDate = normalize(rawValue)
            adyenLog(AdyenLogLevel.VERBOSE) { "getDate - $normalizedExpiryDate" }
            return try {
                val parsedDate = requireNotNull(dateFormat.parse(normalizedExpiryDate))
                val calendar = GregorianCalendar.getInstance()
                calendar.time = parsedDate
                fixCalendarYear(calendar)
                // GregorianCalendar is 0 based
                ExpiryDate(calendar[Calendar.MONTH] + 1, calendar[Calendar.YEAR])
            } catch (e: ParseException) {
                adyenLog(AdyenLogLevel.DEBUG, e) { "getDate - value does not match expected pattern. " }
                if (rawValue.isEmpty()) EMPTY_DATE else INVALID_DATE
            }
        }
        set(expiryDate) {
            if (expiryDate !== EMPTY_DATE) {
                adyenLog(AdyenLogLevel.VERBOSE) { "setDate - " + expiryDate.expiryYear + " " + expiryDate.expiryMonth }
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
        // On SimpleDateFormat, if the truncated (yy) year is more than 20 years in the future it will use the previous
        // century.
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
        const val SEPARATOR = "/"
        private const val DATE_FORMAT = "MM" + SEPARATOR + "yy"
        private const val MAX_LENGTH = 5
        private const val MAX_SECOND_DIGIT_MONTH = 1
    }
}
