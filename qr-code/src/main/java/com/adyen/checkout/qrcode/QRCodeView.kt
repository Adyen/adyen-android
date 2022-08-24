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
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.extensions.copyTextToClipboard
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.qrcode.databinding.QrcodeViewBinding
import java.util.concurrent.TimeUnit

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class QRCodeView :
    AdyenLinearLayout<QRCodeOutputData, QRCodeConfiguration, ActionComponentData, QRCodeComponent>,
    Observer<QRCodeOutputData> {

    private val binding: QrcodeViewBinding = QrcodeViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var imageLoader: ImageLoader
    private var paymentMethodType: String? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onComponentAttached() {
        imageLoader = ImageLoader.getInstance(context, component.configuration.environment)
    }

    override fun initView() {
        binding.copyButton.setOnClickListener { copyCode() }
    }

    private fun copyCode() {
        val code = component.outputData?.qrCodeData ?: return
        context.copyTextToClipboard(
            "Pix Code",
            code,
            localizedContext.getString(R.string.checkout_qr_code_copied_toast)
        )
    }

    override val isConfirmationRequired: Boolean
        get() = false

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.copyButton.setLocalizedTextFromStyle(R.style.AdyenCheckout_QrCode_CopyButton)
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
        component.observeTimer(lifecycleOwner, ::onTimerTick)
    }

    override fun onChanged(outputData: QRCodeOutputData?) {
        Logger.d(TAG, "onChanged")
        if (outputData == null) return

        val type = paymentMethodType
        if (type == null || type != outputData.paymentMethodType) {
            paymentMethodType = outputData.paymentMethodType
            updateMessageText()
            updateLogo()
        }
    }

    private fun onTimerTick(timerData: TimerData) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timerData.millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timerData.millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)
        val minutesSecondsString = localizedContext.getString(
            R.string.checkout_qr_code_time_left_format,
            minutes,
            seconds
        )
        binding.textViewTimer.text = localizedContext.getString(
            R.string.checkout_qr_code_timer_text,
            minutesSecondsString
        )
        binding.progressIndicator.progress = timerData.progress
    }

    private fun updateLogo() {
        Logger.d(TAG, "updateLogo - $paymentMethodType")
        val type = paymentMethodType
        if (!type.isNullOrEmpty()) {
            imageLoader.load(type, binding.imageViewLogo)
        }
    }

    private fun updateMessageText() {
        val resId = getMessageTextResource() ?: return
        binding.textViewTopLabel.text = localizedContext.getString(resId)
    }

    @StringRes
    private fun getMessageTextResource(): Int? {
        return when (paymentMethodType) {
            PaymentMethodTypes.PIX -> R.string.checkout_qr_code_pix
            else -> null
        }
    }
}
