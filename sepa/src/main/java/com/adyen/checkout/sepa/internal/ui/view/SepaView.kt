/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */
package com.adyen.checkout.sepa.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.sepa.R
import com.adyen.checkout.sepa.databinding.SepaViewBinding
import com.adyen.checkout.sepa.internal.ui.SepaDelegate
import com.adyen.checkout.sepa.internal.ui.model.SepaOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.old.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class SepaView @JvmOverloads constructor(
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

    private val binding: SepaViewBinding = SepaViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var sepaDelegate: SepaDelegate

    // Regular View constructor
    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is SepaDelegate) { "Unsupported delegate type" }
        sepaDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        binding.editTextHolderName.setOnChangeListener {
            sepaDelegate.updateInputData { name = binding.editTextHolderName.rawValue }
            binding.textInputLayoutHolderName.hideError()
        }
        binding.editTextIbanNumber.setOnChangeListener {
            sepaDelegate.updateInputData { iban = binding.editTextIbanNumber.rawValue }
            binding.textInputLayoutIbanNumber.hideError()
        }
        binding.editTextIbanNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = sepaDelegate.outputData
            val ibanNumberValidation = outputData.ibanNumberField.validation
            if (hasFocus) {
                binding.textInputLayoutIbanNumber.hideError()
            } else if (!ibanNumberValidation.isValid()) {
                val errorReasonResId = (ibanNumberValidation as Validation.Invalid).reason
                binding.textInputLayoutIbanNumber.showError(localizedContext.getString(errorReasonResId))
            }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutHolderName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Sepa_HolderNameInput,
            localizedContext,
        )
        binding.textInputLayoutIbanNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Sepa_AccountNumberInput,
            localizedContext,
        )
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val outputData: SepaOutputData = sepaDelegate.outputData
        var errorFocused = false
        val ownerNameValidation = outputData.ownerNameField.validation
        if (!ownerNameValidation.isValid()) {
            errorFocused = true
            binding.textInputLayoutHolderName.requestFocus()
            val errorReasonResId = (ownerNameValidation as Validation.Invalid).reason
            binding.textInputLayoutHolderName.showError(localizedContext.getString(errorReasonResId))
        }
        val ibanNumberValidation = outputData.ibanNumberField.validation
        if (!ibanNumberValidation.isValid()) {
            if (!errorFocused) {
                binding.textInputLayoutIbanNumber.requestFocus()
            }
            val errorReasonResId = (ibanNumberValidation as Validation.Invalid).reason
            binding.textInputLayoutIbanNumber.showError(localizedContext.getString(errorReasonResId))
        }
    }

    override fun getView(): View = this
}
