/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/9/2022.
 */

package com.adyen.checkout.qrcode.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.copyTextToClipboard
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.qrcode.R
import com.adyen.checkout.qrcode.databinding.SimpleQrcodeViewBinding
import com.adyen.checkout.qrcode.internal.ui.QRCodeDelegate
import com.adyen.checkout.qrcode.internal.ui.model.QRCodeOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.load
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class SimpleQRCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr,
    ),
    ComponentView {

    private val binding: SimpleQrcodeViewBinding = SimpleQrcodeViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: QRCodeDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is QRCodeDelegate) { "Unsupported delegate type" }

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
        adyenLog(AdyenLogLevel.DEBUG) { "outputDataChanged" }

        updateMessageText(outputData.paymentMethodType)
        updateLogo(outputData.paymentMethodType)
        updateQrImage(outputData.qrImageUrl)
        updateAmount(delegate.componentParams)
    }

    private fun updateMessageText(paymentMethodType: String?) {
        val resId = getMessageTextResource(paymentMethodType) ?: return
        binding.textViewTopLabel.text = localizedContext.getString(resId)
    }

    @StringRes
    private fun getMessageTextResource(paymentMethodType: String?): Int? {
        return when (paymentMethodType) {
            PaymentMethodTypes.PIX -> R.string.checkout_qr_code_pix
            else -> null
        }
    }

    private fun updateLogo(paymentMethodType: String?) {
        adyenLog(AdyenLogLevel.DEBUG) { "updateLogo - $paymentMethodType" }
        if (!paymentMethodType.isNullOrEmpty()) {
            binding.imageViewLogo.loadLogo(
                environment = delegate.componentParams.environment,
                txVariant = paymentMethodType,
            )
        }
    }

    private fun updateQrImage(qrImageUrl: String?) {
        if (!qrImageUrl.isNullOrEmpty()) {
            binding.imageViewQrcode.load(url = qrImageUrl)
        }
    }

    private fun updateAmount(componentParams: ComponentParams) {
        val amount = componentParams.amount
        if (amount != null) {
            val formattedAmount = CurrencyUtils.formatAmount(
                amount,
                componentParams.shopperLocale,
            )
            binding.textviewAmount.isVisible = true
            binding.textviewAmount.text = formattedAmount
        } else {
            binding.textviewAmount.isVisible = false
        }
    }

    private fun onTimerTick(timerData: TimerData) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timerData.millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timerData.millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

        val minutesSecondsString = localizedContext.getString(
            R.string.checkout_qr_code_time_left_format,
            minutes,
            seconds,
        )

        binding.textViewTimer.text = localizedContext.getString(
            R.string.checkout_qr_code_timer_text,
            minutesSecondsString,
        )
        binding.progressIndicator.progress = timerData.progress
    }

    private fun copyCode(qrCodeData: String?) {
        qrCodeData ?: return
        context.copyTextToClipboard(
            "Pix Code",
            qrCodeData,
            localizedContext.getString(R.string.checkout_qr_code_copied_toast),
        )
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit
}
