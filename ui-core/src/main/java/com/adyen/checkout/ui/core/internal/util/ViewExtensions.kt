@file:Suppress("TooManyFunctions")

package com.adyen.checkout.ui.core.internal.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RestrictTo
import androidx.annotation.StyleRes
import androidx.core.view.doOnNextLayout
import com.google.android.material.textfield.TextInputLayout

@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@set:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline var TextInputLayout.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        val visibility = if (value) View.VISIBLE else View.GONE
        this.visibility = visibility
        editText?.apply {
            this.visibility = visibility
            isFocusable = value
            isFocusableInTouchMode = value
        }
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputLayout.setLocalizedHintFromStyle(@StyleRes styleResId: Int, localizedContext: Context) {
    val attrs = intArrayOf(android.R.attr.hint)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    hint = typedArray.getString(0)
    typedArray.recycle()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextView.setLocalizedTextFromStyle(
    @StyleRes styleResId: Int,
    localizedContext: Context,
    formatHyperLink: Boolean = false
) {
    val attrs = intArrayOf(android.R.attr.text)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    val stringResValue = typedArray.getString(0).orEmpty()
    text = if (formatHyperLink) stringResValue.formatStringWithHyperlink() else stringResValue
    typedArray.recycle()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun SearchView.setLocalizedQueryHintFromStyle(@StyleRes styleResId: Int, localizedContext: Context) {
    val attrs = intArrayOf(android.R.attr.queryHint)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    queryHint = typedArray.getString(0)
    typedArray.recycle()
}

internal fun String.formatStringWithHyperlink(replacementToken: String = "%#"): CharSequence {
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun String.formatFullStringWithHyperLink(): CharSequence {
    return SpannableString(this).apply {
        setSpan(URLSpan(""), 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputLayout.showError(error: String) {
    isErrorEnabled = true
    this.error = error
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputLayout.hideError() {
    error = null
    isErrorEnabled = false
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(this, SHOW_IMPLICIT)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

internal fun View.resetFocus() {
    requestFocus()
    clearFocus()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.requestFocusOnNextLayout() {
    doOnNextLayout { view ->
        view.requestFocus()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun EditText.setAccessibilityDelegateWith(contentDescription: String) {
    accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.contentDescription = contentDescription
        }
    }
}
