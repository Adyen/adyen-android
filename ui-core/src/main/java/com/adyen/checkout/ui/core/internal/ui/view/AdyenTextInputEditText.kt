/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/8/2019.
 */
package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.UIEventDelegate
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

open class AdyenTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(context, attrs, if (defStyleAttr == 0) R.attr.editTextStyle else defStyleAttr) {

    private var listener: Listener? = null

    open val rawValue: String
        get() = text?.toString() ?: ""

    /**
     * Constructor of AdyenTextInputEditText.
     */
    init {
        this.addTextChangedListener(textWatcher)
    }

    fun setOnChangeListener(listener: Listener?) {
        this.listener = listener
    }

    @CallSuper
    protected open fun afterTextChanged(editable: Editable) {
        // TODO temporary measure to avoid notifying changes for disabled edit texts.
        //  This should be removed after the view model starts emitting the final formatted strings for the edit texts
        if (isEnabled) listener?.onTextChanged(editable)
    }

    protected fun enforceMaxInputLength(maxLength: Int) {
        if (maxLength != NO_MAX_LENGTH) {
            filters = arrayOf<InputFilter>(LengthFilter(maxLength))
        }
    }

    private val textWatcher: TextWatcher
        get() = object : TextWatcher {

            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (!isEditing) {
                    isEditing = true
                    this@AdyenTextInputEditText.afterTextChanged(s)
                    isEditing = false
                }
            }
        }

    fun interface Listener {
        fun onTextChanged(editable: Editable)
    }

    companion object {
        protected const val NO_MAX_LENGTH = -1
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <FI> AdyenTextInputEditText.safeUpdateText(
    delegate: UIEventDelegate<FI>,
    fieldId: FI,
    value: String,
) {
    setOnChangeListener(null)
    onFocusChangeListener = null

    setText(value)
    setSelection(length())

    setOnChangeListener {
        delegate.onFieldValueChanged(fieldId, it.toString())
    }
    onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
        delegate.onFieldFocusChanged(fieldId, hasFocus)
    }
}
