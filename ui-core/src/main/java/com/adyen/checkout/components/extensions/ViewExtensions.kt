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
    formatHyperLink: Boolean = false
) {
    val attrs = intArrayOf(android.R.attr.text)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    val stringResValue = typedArray.getString(0).orEmpty()
    text = if (formatHyperLink) stringResValue.formatStringWithHyperlink()
    else stringResValue
    typedArray.recycle()
}

fun String.formatStringWithHyperlink(replacementToken: String = "%#"): CharSequence {
    // check if the string contains the replacement token twice
    val counter = this.split(replacementToken).size - 1
    if (counter != 2) return this
    val firstTokenIndex = this.indexOf(replacementToken, 0, ignoreCase = true)
    val lastTokenIndex = this.lastIndexOf(replacementToken) - replacementToken.length

    val sanitizedText = this.replace(replacementToken, "", ignoreCase = true)

    return SpannableString(sanitizedText).apply {
        setSpan(URLSpan(""), firstTokenIndex, lastTokenIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
