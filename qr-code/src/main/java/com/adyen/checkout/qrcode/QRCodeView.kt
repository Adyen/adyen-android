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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.extensions.copyTextToClipboard
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

class QRCodeView : AdyenLinearLayout<QRCodeOutputData, QRCodeConfiguration, ActionComponentData, QRCodeComponent>, Observer<QRCodeOutputData> {

    private lateinit var imageView: ImageView
    private lateinit var topLabelTextView: TextView
    private lateinit var copyButton: Button

    private lateinit var imageLoader: ImageLoader
    private var paymentMethodType: String? = null

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
        val configuration = component.configuration
            ?: throw ComponentException("Configuration cannot be null")
        imageLoader = ImageLoader.getInstance(context, configuration.environment)
    }

    override fun initView() {
        imageView = findViewById(R.id.imageView_logo)
        topLabelTextView = findViewById(R.id.textView_top_label)
        copyButton = findViewById(R.id.copyButton)
        copyButton.setOnClickListener { copyCode() }
    }

    private fun copyCode() {
        val code = component.getCodeString() ?: return
        context.copyTextToClipboard("Pix Code", code, "Code copied to clipboard")
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

        val type = paymentMethodType
        if (type == null || type != outputData.paymentMethodType) {
            paymentMethodType = outputData.paymentMethodType
            updateLogo()
        }
    }

    private fun updateLogo() {
        Logger.d(TAG, "updateLogo - $paymentMethodType")
        val type = paymentMethodType
        if (!type.isNullOrEmpty()) {
            imageLoader.load(type, imageView)
        }
    }

}