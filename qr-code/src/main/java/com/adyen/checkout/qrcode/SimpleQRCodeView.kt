/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/9/2022.
 */

package com.adyen.checkout.qrcode

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.copyTextToClipboard
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.image.loadLogo
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.qrcode.databinding.SimpleQrcodeViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
internal class SimpleQRCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: SimpleQrcodeViewBinding = SimpleQrcodeViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: QRCodeDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is QRCodeDelegate) throw IllegalStateException("Unsupported delegate type")

        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        binding.copyButton.setOnClickListener { copyCode(delegate.outputData.qrCodeData) }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.copyButton.setLocalizedTextFromStyle(R.style.AdyenCheckout_QrCode_CopyButton, localizedContext)
    }

    private fun observeDelegate(delegate: QRCodeDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)

        delegate.timerFlow
            .onEach { onTimerTick(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: QRCodeOutputData) {
        Logger.d(TAG, "outputDataChanged")

        updateMessageText(outputData.paymentMethodType)
        updateLogo(outputData.paymentMethodType)
    }

    @StringRes
    private fun getMessageTextResource(paymentMethodType: String?): Int? {
        return when (paymentMethodType) {
            PaymentMethodTypes.PIX -> R.string.checkout_qr_code_pix
            else -> null
        }
    }

    private fun updateLogo(paymentMethodType: String?) {
        Logger.d(TAG, "updateLogo - $paymentMethodType")
        if (!paymentMethodType.isNullOrEmpty()) {
            binding.imageViewLogo.loadLogo(
                environment = delegate.componentParams.environment,
                txVariant = paymentMethodType,
            )
        }
    }

    private fun updateMessageText(paymentMethodType: String?) {
        val resId = getMessageTextResource(paymentMethodType) ?: return
        binding.textViewTopLabel.text = localizedContext.getString(resId)
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

    private fun copyCode(qrCodeData: String?) {
        qrCodeData ?: return
        context.copyTextToClipboard(
            "Pix Code",
            qrCodeData,
            localizedContext.getString(R.string.checkout_qr_code_copied_toast)
        )
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
