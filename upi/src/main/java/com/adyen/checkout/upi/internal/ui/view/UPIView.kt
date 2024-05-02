/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import com.adyen.checkout.ui.core.internal.util.showKeyboard
import com.adyen.checkout.upi.R
import com.adyen.checkout.upi.databinding.UpiViewBinding
import com.adyen.checkout.upi.internal.ui.UPIDelegate
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class UPIView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = UpiViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: UPIDelegate

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is UPIDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)

        initPicker(delegate)
        initVpaInput(delegate, localizedContext)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewModeSelection.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_ModeSelectionTextView,
            localizedContext,
        )
        binding.buttonVpa.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_VPAButton,
            localizedContext,
        )
        binding.buttonQrCode.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_QRButton,
            localizedContext,
        )
        binding.textInputLayoutVpa.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_UPI_VPAEditText,
            localizedContext,
        )
        binding.textViewQrCodeDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_QRGenerationTextView,
            localizedContext,
        )
    }

    private fun initPicker(delegate: UPIDelegate) {
        binding.toggleButtonChoice.check(R.id.button_vpa)
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_vpa -> {
                    binding.textInputLayoutVpa.isVisible = isChecked
                    binding.textViewQrCodeDescription.isVisible = !isChecked
                    binding.editTextVpa.isFocusableInTouchMode = isChecked
                    binding.editTextVpa.isFocusable = isChecked
                    if (isChecked) {
                        binding.editTextVpa.requestFocus()
                        binding.editTextVpa.showKeyboard()
                        delegate.updateInputData { mode = UPIMode.VPA }
                    }
                }

                R.id.button_qrCode -> {
                    binding.textInputLayoutVpa.isVisible = !isChecked
                    binding.textViewQrCodeDescription.isVisible = isChecked
                    binding.editTextVpa.isFocusableInTouchMode = !isChecked
                    binding.editTextVpa.isFocusable = !isChecked
                    if (isChecked) {
                        binding.editTextVpa.clearFocus()
                        hideKeyboard()
                        delegate.updateInputData { mode = UPIMode.QR }
                    }
                }
            }
        }
    }

    private fun initVpaInput(delegate: UPIDelegate, localizedContext: Context) {
        binding.editTextVpa.setOnChangeListener { editable ->
            delegate.updateInputData { virtualPaymentAddress = editable.toString() }
            binding.textInputLayoutVpa.hideError()
        }

        binding.editTextVpa.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val outputData = delegate.outputData

            val vpaValidation = outputData.virtualPaymentAddressFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutVpa.hideError()
            } else if (vpaValidation is Validation.Invalid) {
                binding.textInputLayoutVpa.showError(localizedContext.getString(vpaValidation.reason))
            }
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val vpaValidation = delegate.outputData.virtualPaymentAddressFieldState.validation
        if (vpaValidation is Validation.Invalid) {
            binding.textInputLayoutVpa.showError(localizedContext.getString(vpaValidation.reason))
        }
    }

    override fun getView(): View = this
}
