/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.autofill.HintConstants
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.old.internal.util.showError
import com.adyen.checkout.ui.core.old.internal.util.showKeyboard
import com.adyen.checkout.upi.R
import com.adyen.checkout.upi.databinding.UpiViewBinding
import com.adyen.checkout.upi.internal.ui.UPIDelegate
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import com.adyen.checkout.upi.internal.ui.model.UPISelectedMode
import com.adyen.checkout.upi.internal.ui.model.mapToSelectedMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
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

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is UPIDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)
        initModeToggle()
        initVpaInput(delegate, localizedContext)
        observeDelegate(delegate, coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewModeSelection.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_ModeSelectionTextView,
            localizedContext,
        )
        binding.buttonIntent.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_IntentButton,
            localizedContext,
        )
        binding.buttonVpa.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_VPAButton,
            localizedContext,
        )
        binding.textViewIntentInstruction.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_IntentInstructionTextView,
            localizedContext,
        )
        binding.textViewVpaInstruction.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_VPAInstructionTextView,
            localizedContext,
        )
        binding.textViewNoAppSelected.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_UPI_NoAppSelectedTextView,
            localizedContext,
        )
        binding.textInputLayoutVpa.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_UPI_VPAEditText,
            localizedContext,
        )
    }

    private fun initModeToggle() {
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_intent -> updateUpiIntentViews(isChecked)
                R.id.button_vpa -> updateUpiVpaViews(isChecked)
            }
        }
    }

    private fun observeDelegate(delegate: UPIDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: UPIOutputData) {
        initPicker(outputData.availableModes, outputData.selectedMode)
        initError(outputData)
    }

    private fun initPicker(availableModes: List<UPIMode>, selectedMode: UPISelectedMode) {
        val shouldShowPicker = availableModes.size > 1
        binding.textViewModeSelection.isVisible = shouldShowPicker
        binding.toggleButtonChoice.isVisible = shouldShowPicker
        availableModes.forEach { mode ->
            initViewsForMode(mode = mode, isChecked = mode.mapToSelectedMode() == selectedMode)
        }
    }

    private fun initViewsForMode(mode: UPIMode, isChecked: Boolean) {
        when (mode) {
            is UPIMode.Intent -> {
                initViewsForIntent(mode, isChecked)
            }

            is UPIMode.Vpa -> {
                initViewsForVpa(isChecked)
            }
        }
    }

    private fun initViewsForIntent(mode: UPIMode.Intent, isChecked: Boolean) = with(binding) {
        buttonIntent.isVisible = true
        if (isChecked) {
            toggleButtonChoice.check(R.id.button_intent)
        }

        if (upiAppsAdapter == null) {
            upiAppsAdapter = UPIAppsAdapter(
                context = context,
                paymentMethod = delegate.getPaymentMethodType(),
                onItemClickListener = ::onIntentItemClicked,
            )
            recyclerViewUpiIntent.adapter = upiAppsAdapter
        }
        upiAppsAdapter?.submitList(mode.intentItems)
    }

    private fun updateUpiIntentViews(isChecked: Boolean) {
        binding.textViewIntentInstruction.isVisible = isChecked
        binding.recyclerViewUpiIntent.isVisible = isChecked
        if (isChecked) {
            binding.editTextVpa.clearFocus()
            hideKeyboard()
            delegate.updateInputData { selectedMode = UPISelectedMode.INTENT }
        }
    }

    private fun onIntentItemClicked(item: UPIIntentItem) {
        delegate.updateInputData {
            selectedUPIIntentItem = item
        }
    }

    private fun initViewsForVpa(isChecked: Boolean) = with(binding) {
        buttonVpa.isVisible = true
        if (isChecked) {
            toggleButtonChoice.check(R.id.button_vpa)
        }
    }

    private fun updateUpiVpaViews(isChecked: Boolean) {
        binding.textViewVpaInstruction.isVisible = isChecked
        binding.textInputLayoutVpa.isVisible = isChecked
        binding.editTextVpa.isFocusableInTouchMode = isChecked
        binding.editTextVpa.isFocusable = isChecked
        if (isChecked) {
            binding.editTextVpa.requestFocus()
            binding.editTextVpa.showKeyboard()
            delegate.updateInputData { selectedMode = UPISelectedMode.VPA }
        }
    }

    private fun initError(outputData: UPIOutputData) {
        binding.textViewNoAppSelected.isVisible = outputData.showNoSelectedUPIIntentItemError
    }

    private fun initVpaInput(delegate: UPIDelegate, localizedContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.editTextVpa.setAutofillHints(HintConstants.AUTOFILL_HINT_UPI_VPA)
        }
        binding.editTextVpa.setOnChangeListener { editable ->
            delegate.updateInputData { vpaVirtualPaymentAddress = editable.toString() }
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

        delegate.highlightValidationErrors()
    }

    override fun getView(): View = this
}
