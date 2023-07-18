/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 16/11/2022.
 */

package com.adyen.checkout.qrcode.internal.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.isEmpty
import com.adyen.checkout.core.exception.PermissionException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.qrcode.R
import com.adyen.checkout.qrcode.databinding.FullQrcodeViewBinding
import com.adyen.checkout.qrcode.internal.ui.QRCodeDelegate
import com.adyen.checkout.qrcode.internal.ui.model.QRCodeOutputData
import com.adyen.checkout.qrcode.internal.ui.model.QrCodeUIEvent
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.LogoSize
import com.adyen.checkout.ui.core.internal.ui.load
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
internal class FullQRCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: FullQrcodeViewBinding = FullQrcodeViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: QRCodeDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is QRCodeDelegate) { "Unsupported delegate type" }

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        this.delegate = delegate

        observeDelegate(delegate, coroutineScope)

        binding.buttonSaveImage.setOnClickListener {
            val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, requiredPermission) != PackageManager.PERMISSION_GRANTED
            ) {
                delegate.onError(
                    PermissionException(
                        errorMessage = "$requiredPermission permission is not granted",
                        requiredPermission = requiredPermission
                    )
                )
                return@setOnClickListener
            }
            delegate.downloadQRImage()
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.buttonSaveImage.setLocalizedTextFromStyle(R.style.AdyenCheckout_QrCode_SaveButton, localizedContext)
    }

    private fun observeDelegate(delegate: QRCodeDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)

        delegate.timerFlow
            .onEach { onTimerTick(it) }
            .launchIn(coroutineScope)

        delegate.eventFlow
            .onEach { handleEventFlow(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: QRCodeOutputData) {
        Logger.d(TAG, "outputDataChanged")

        updateMessageText(outputData.messageTextResource)
        updateLogo(outputData.paymentMethodType)
        updateQrImage(outputData.qrImageUrl)
        updateAmount(delegate.componentParams)
    }

    private fun updateAmount(componentParams: ComponentParams) {
        if (!componentParams.amount.isEmpty) {
            val formattedAmount = CurrencyUtils.formatAmount(
                componentParams.amount,
                componentParams.shopperLocale
            )
            binding.textviewAmount.isVisible = true
            binding.textviewAmount.text = formattedAmount
        } else {
            binding.textviewAmount.isVisible = false
        }
    }

    private fun updateMessageText(@StringRes messageTextResource: Int?) {
        if (messageTextResource == null) return
        binding.textViewTopLabel.text = localizedContext.getString(messageTextResource)
    }

    private fun updateLogo(paymentMethodType: String?) {
        if (!paymentMethodType.isNullOrEmpty()) {
            binding.imageViewLogo.loadLogo(
                environment = delegate.componentParams.environment,
                txVariant = paymentMethodType,
                size = LogoSize.LARGE
            )
        }
    }

    private fun updateQrImage(qrImageUrl: String?) {
        if (!qrImageUrl.isNullOrEmpty()) {
            binding.imageViewQrcode.load(url = qrImageUrl)
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
            R.string.checkout_qr_code_pay_now_timer_text,
            minutesSecondsString
        )
        binding.progressIndicator.progress = timerData.progress
    }

    private fun handleEventFlow(event: QrCodeUIEvent) {
        when (event) {
            QrCodeUIEvent.QrImageDownloadResult.Success -> {
                context.toast(localizedContext.getString(R.string.checkout_qr_code_download_image_succeeded))
            }
            is QrCodeUIEvent.QrImageDownloadResult.Failure -> {
                context.toast(localizedContext.getString(R.string.checkout_qr_code_download_image_failed))
                Logger.e(TAG, "download file failed", event.throwable)
            }
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
