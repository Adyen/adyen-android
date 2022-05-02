package com.adyen.checkout.components.extensions

import android.content.Context
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

fun TextView.setLocalizedTextFromStyle(@StyleRes styleResId: Int, localizedContext: Context) {
    val attrs = intArrayOf(android.R.attr.text)
    val typedArray = localizedContext.obtainStyledAttributes(styleResId, attrs)
    text = typedArray.getString(0)
    typedArray.recycle()
}
