/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.google.android.material.textfield.TextInputLayout

class BlikView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenLinearLayout<BlikOutputData, BlikConfiguration, PaymentComponentState<BlikPaymentMethod>, BlikComponent>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<BlikOutputData> {

    private lateinit var blikHeader: TextView
    private lateinit var blikCodeInput: TextInputLayout
    private lateinit var blikCodeEditText: AdyenTextInputEditText

    init {
        orientation = VERTICAL
        LayoutInflater.from(getContext()).inflate(R.layout.blik_view, this, true)
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        blikCodeInput.setLocalizedHintFromStyle(R.style.AdyenCheckout_Blik_BlikCodeInput)
        blikHeader.setLocalizedTextFromStyle(R.style.AdyenCheckout_Blik_BlikHeaderTextView)
    }

    override fun initView() {
        blikHeader = findViewById(R.id.textView_blikHeader)
        blikCodeInput = findViewById(R.id.textInputLayout_blikCode)
        blikCodeEditText = blikCodeInput.editText as AdyenTextInputEditText

        blikCodeEditText.setOnChangeListener {
            component.inputData.blikCode = blikCodeEditText.rawValue
            notifyInputDataChanged()
            blikCodeInput.error = null
        }

        blikCodeEditText.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            val blikCodeValidation = outputData?.blikCodeField?.validation
            if (hasFocus) {
                blikCodeInput.error = null
            } else if (blikCodeValidation != null && !blikCodeValidation.isValid()) {
                val errorReasonResId = (blikCodeValidation as Validation.Invalid).reason
                blikCodeInput.error = localizedContext.getString(errorReasonResId)
            }
        }
    }

    override fun onChanged(blikOutputData: BlikOutputData?) {
        Logger.v(TAG, "blikOutputData changed")
    }

    override fun onComponentAttached() = Unit // nothing to impl

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData = component.outputData ?: return
        val blikCodeValidation = outputData.blikCodeField.validation
        if (!blikCodeValidation.isValid()) {
            blikCodeInput.requestFocus()
            val errorReasonResId = (blikCodeValidation as Validation.Invalid).reason
            blikCodeInput.error = localizedContext.getString(errorReasonResId)
        }
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
