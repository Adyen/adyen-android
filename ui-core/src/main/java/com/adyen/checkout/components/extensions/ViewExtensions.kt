package com.adyen.checkout.components.extensions

import android.view.View
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
