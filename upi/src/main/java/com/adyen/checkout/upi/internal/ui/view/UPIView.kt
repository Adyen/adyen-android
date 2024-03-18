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
import com.adyen.checkout.upi.internal.ui.model.UPIApp
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import kotlinx.coroutines.CoroutineScope

internal class UPIView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = UpiViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: UPIDelegate

    private lateinit var localizedContext: Context

    private var upiAppsAdapter: UPIAppsAdapter? = null

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is UPIDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)

        initPicker(delegate.outputData.availableModes, delegate.outputData.checkedMode)
        // TODO: Check if we need this
        initVpaInput(delegate, localizedContext)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewModeSelection.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_ModeSelectionTextView,
            localizedContext,
        )
        binding.buttonCollect.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_CollectButton,
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


    private fun initPicker(availableModes: List<UPIMode>, checkedMode: UPIMode) {
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_collect -> updateUpiCollectViews(isChecked)
                R.id.button_vpa -> updateUpiVpaViews(isChecked)
                R.id.button_qrCode -> updateUpiQrCodeViews(isChecked)
            }
        }

        availableModes.forEach { mode ->
            initViewsForMode(mode = mode, isChecked = mode == checkedMode)
        }
    }

    private fun initViewsForMode(mode: UPIMode, isChecked: Boolean) {
        when (mode) {
            is UPIMode.Collect -> {
                initViewsForCollect(mode.apps, isChecked)
            }

            is UPIMode.Vpa -> {
                initViewsForVpa(isChecked)
            }

            is UPIMode.Qr -> {
                initViewsForQr(isChecked)
            }
        }
    }

    private fun initViewsForCollect(apps: List<UPIApp>, isChecked: Boolean) = with(binding) {
        buttonCollect.isVisible = true
        if (isChecked) {
            toggleButtonChoice.check(R.id.button_collect)
        }

        if (upiAppsAdapter == null) {
            upiAppsAdapter = UPIAppsAdapter(
                context = context,
                paymentMethod = delegate.getPaymentMethodType()
            )
            recyclerViewUpiCollect.adapter = upiAppsAdapter
        }
        upiAppsAdapter?.setItems(apps)
    }

    private fun initViewsForVpa(isChecked: Boolean) = with(binding) {
        buttonVpa.isVisible = true
        if (isChecked) {
            toggleButtonChoice.check(R.id.button_vpa)
        }
    }

    private fun initViewsForQr(isChecked: Boolean) = with(binding) {
        buttonQrCode.isVisible = true
        if (isChecked) {
            toggleButtonChoice.check(R.id.button_qrCode)
        }
    }

    private fun updateUpiCollectViews(isChecked: Boolean) {
//        resetUpiModeViews()
        binding.recyclerViewUpiCollect.isVisible = isChecked
        if (isChecked) {
            // TODO: This should work differently
//            delegate.updateInputData { mode = COLLECT }
            binding.editTextVpa.clearFocus()
            hideKeyboard()
        }
    }

    private fun updateUpiVpaViews(isChecked: Boolean) {
//        resetUpiModeViews()
        binding.textInputLayoutVpa.isVisible = isChecked
        binding.editTextVpa.isFocusableInTouchMode = isChecked
        binding.editTextVpa.isFocusable = isChecked
        if (isChecked) {
            binding.editTextVpa.requestFocus()
            binding.editTextVpa.showKeyboard()
//            delegate.updateInputData { mode = VPA }
        }
    }

    private fun updateUpiQrCodeViews(isChecked: Boolean) {
//        resetUpiModeViews()
        binding.textViewQrCodeDescription.isVisible = isChecked
        if (isChecked) {
//            delegate.updateInputData { mode = QR }
            binding.editTextVpa.clearFocus()
            hideKeyboard()
        }
    }

    private fun resetUpiModeViews() {
        binding.recyclerViewUpiCollect.isVisible = false
        binding.textInputLayoutVpa.isVisible = false
        binding.textViewQrCodeDescription.isVisible = false
        binding.editTextVpa.isFocusableInTouchMode = false
        binding.editTextVpa.isFocusable = false
        binding.editTextVpa.clearFocus()
        hideKeyboard()
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
