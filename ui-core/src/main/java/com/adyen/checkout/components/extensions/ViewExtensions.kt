package com.adyen.checkout.components.extensions

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import com.google.android.material.textfield.TextInputLayout

inline var TextInputLayout.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        val visibility = if (value) View.VISIBLE else View.GONE
        this.visibility = visibility
        editText?.apply {
            this.visibility = visibility
            isFocusable = value
        }
    }

fun TextInputLayout.setLocalizedHintFromStyle(@StyleRes styleResId: Int, localizedContext: Context) {
    val attrs = intArrayOf(android.R.attr.hint)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    hint = typedArray.getString(0)
    typedArray.recycle()
}

fun TextView.setLocalizedTextFromStyle(
    @StyleRes styleResId: Int,
    localizedContext: Context,
    formatHyperLink: Boolean = false,
    replacementToken: String = ""
) {
    val attrs = intArrayOf(android.R.attr.text)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    val stringResValue = typedArray.getString(0)
    // check if the string contains the replacement token twice
    val counter = stringResValue?.split(replacementToken)?.size?.minus(1)
    text = if (formatHyperLink && counter == 2) stringResValue.formatStringWithHyperlink(replacementToken)
    else stringResValue
    typedArray.recycle()
}

fun String.formatStringWithHyperlink(replacementToken: String): SpannableString {
    val firstTokenIndex = this.indexOf(replacementToken, 0, ignoreCase = true)
    val lastTokenIndex = this.lastIndexOf(replacementToken) - replacementToken.length

    val sanitizedText = this.replace(replacementToken, "", ignoreCase = true)

    return SpannableString(sanitizedText).apply {
        setSpan(URLSpan(""), firstTokenIndex, lastTokenIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
