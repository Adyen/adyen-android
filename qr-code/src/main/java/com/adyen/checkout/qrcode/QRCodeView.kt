/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */

package com.adyen.checkout.qrcode

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

class QRCodeView : AdyenLinearLayout<QRCodeOutputData, QRCodeConfiguration, ActionComponentData, QRCodeComponent>, Observer<QRCodeOutputData> {

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

        LayoutInflater.from(context).inflate(R.layout.qrcode_view, this, true)

        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onComponentAttached() {
    }

    override fun initView() {
    }

    override fun isConfirmationRequired(): Boolean = false

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun initLocalizedStrings(localizedContext: Context) {
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(outputData: QRCodeOutputData?) {
        Logger.d(TAG, "onChanged")
        if (outputData == null) return
    }

}