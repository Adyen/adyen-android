/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2023.
 */

package com.adyen.checkout.upi

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.internal.util.showError
import com.adyen.checkout.ui.core.internal.util.showKeyboard
import com.adyen.checkout.upi.databinding.UpiViewBinding
import kotlinx.coroutines.CoroutineScope

internal class UpiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = UpiViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: UpiDelegate

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is UpiDelegate) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate
        this.localizedContext = localizedContext

        initPicker(delegate)
        initVpaInput(delegate, localizedContext)
    }

    private fun initPicker(delegate: UpiDelegate) {
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
                        delegate.updateInputData { mode = UpiMode.VPA }
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
                        delegate.updateInputData { mode = UpiMode.QR }
                    }
                }
            }
        }
    }

    private fun initVpaInput(delegate: UpiDelegate, localizedContext: Context) {
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
        Logger.d(TAG, "highlightValidationErrors")
        val vpaValidation = delegate.outputData.virtualPaymentAddressFieldState.validation
        if (vpaValidation is Validation.Invalid) {
            binding.textInputLayoutVpa.showError(localizedContext.getString(vpaValidation.reason))
        }
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
