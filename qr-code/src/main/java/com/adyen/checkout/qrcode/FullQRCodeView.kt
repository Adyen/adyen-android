/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 16/11/2022.
 */

package com.adyen.checkout.qrcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.extensions.toast
import com.adyen.checkout.components.image.LogoSize
import com.adyen.checkout.components.image.load
import com.adyen.checkout.components.image.loadLogo
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.components.util.isEmpty
import com.adyen.checkout.core.exception.PermissionException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.qrcode.databinding.FullQrcodeViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is QRCodeDelegate) throw IllegalStateException("Unsupported delegate type")

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
                        errorMessage = "storage permission is not granted",
                        requiredPermission = requiredPermission
                    )
                )
                return@setOnClickListener
            }
            saveQrImage(coroutineScope)
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
    }

    private fun outputDataChanged(outputData: QRCodeOutputData) {
        Logger.d(TAG, "outputDataChanged")

        updateMessageText(outputData.paymentMethodType)
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

    private fun updateMessageText(paymentMethodType: String?) {
        val resId = getMessageTextResource(paymentMethodType) ?: return
        binding.textViewTopLabel.text = localizedContext.getString(resId)
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

    @StringRes
    private fun getMessageTextResource(paymentMethodType: String?): Int? {
        return when (paymentMethodType) {
            PaymentMethodTypes.PAY_NOW -> R.string.checkout_qr_code_pay_now
            else -> null
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

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun saveQrImage(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            delegate.downloadQRImage().fold(
                onSuccess = { context.toast("qr code downloaded successfully") },
                onFailure = { e -> Logger.e(TAG, "download file failed", e) }
            )
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
