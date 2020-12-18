/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/12/2020.
 */

package com.adyen.checkout.mbway

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.mbway.ui.R
import com.google.android.material.textfield.TextInputLayout

class MBWayView :
    AdyenLinearLayout<MBWayOutputData, MBWayConfiguration, PaymentComponentState<MBWayPaymentMethod>, MBWayComponent>,
    Observer<MBWayOutputData> {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private var mMBWayInputData = MBWayInputData()

    private var mMobileNumberInput: TextInputLayout? = null

    private var mMobileNumberEditText: AdyenTextInputEditText? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init()
    }

    private fun init() {
        orientation = VERTICAL

        LayoutInflater.from(context).inflate(R.layout.mbway_view, this, true)

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        val myAttrs = intArrayOf(android.R.attr.hint)

        val typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_MBWay_MobileNumberInput, myAttrs)
        mMobileNumberInput?.hint = typedArray.getString(0)
        typedArray.recycle()
    }

    override fun initView() {
        mMobileNumberInput = findViewById(R.id.textInputLayout_mobileNumber)
        mMobileNumberEditText = (mMobileNumberInput?.editText as? AdyenTextInputEditText)
        val mMobileNumberEditText = mMobileNumberEditText
        val mMobileNumberInput = mMobileNumberInput
        if (mMobileNumberEditText == null || mMobileNumberInput == null) {
            throw CheckoutException("Could not find views inside layout.")
        }
        mMobileNumberEditText.setOnChangeListener {
            mMBWayInputData.mobilePhoneNumber = mMobileNumberEditText.rawValue
            notifyInputDataChanged()
            mMobileNumberInput.error = null
        }
        mMobileNumberEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                mMobileNumberInput.error = null
            } else if (outputData != null && !outputData.mobilePhoneNumberField.isValid) {
                mMobileNumberInput.error = mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid)
            }
        }
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onComponentAttached() {
        // nothing to impl
    }

    override fun onChanged(mbWayOutputData: MBWayOutputData?) {
        Logger.v(TAG, "MBWayOutputData changed")
    }

    override fun isConfirmationRequired(): Boolean {
        return true
    }

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData: MBWayOutputData = component.outputData ?: return
        if (!outputData.mobilePhoneNumberField.isValid) {
            mMobileNumberInput?.error = mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid)
        }
    }

    internal fun notifyInputDataChanged() {
        component.inputDataChanged(mMBWayInputData)
    }
}
