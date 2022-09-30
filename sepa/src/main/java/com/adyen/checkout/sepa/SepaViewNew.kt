/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */
package com.adyen.checkout.sepa

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sepa.databinding.SepaViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SepaViewNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentViewNew {

    private val binding: SepaViewBinding = SepaViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var sepaDelegate: SepaDelegate

    // Regular View constructor
    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is SepaDelegate) throw IllegalArgumentException("Unsupported delegate type")
        sepaDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        binding.editTextHolderName.setOnChangeListener {
            sepaDelegate.inputData.name = binding.editTextHolderName.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutHolderName.error = null
        }
        binding.editTextIbanNumber.setOnChangeListener {
            sepaDelegate.inputData.iban = binding.editTextIbanNumber.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutIbanNumber.error = null
        }
        binding.editTextIbanNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = sepaDelegate.outputData
            val ibanNumberValidation = outputData?.ibanNumberField?.validation
            if (hasFocus) {
                binding.textInputLayoutIbanNumber.error = null
            } else if (ibanNumberValidation != null && !ibanNumberValidation.isValid()) {
                val errorReasonResId = (ibanNumberValidation as Validation.Invalid).reason
                binding.textInputLayoutIbanNumber.error = localizedContext.getString(errorReasonResId)
            }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutHolderName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Sepa_HolderNameInput,
            localizedContext
        )
        binding.textInputLayoutIbanNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Sepa_AccountNumberInput,
            localizedContext
        )
    }

    private fun observeDelegate(delegate: SepaDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(sepaOutputData: SepaOutputData?) {
        Logger.v(TAG, "sepaOutputData changed")
        // no ops
    }

    override val isConfirmationRequired: Boolean = true

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData: SepaOutputData = sepaDelegate.outputData ?: return
        var errorFocused = false
        val ownerNameValidation = outputData.ownerNameField.validation
        if (!ownerNameValidation.isValid()) {
            errorFocused = true
            binding.textInputLayoutHolderName.requestFocus()
            val errorReasonResId = (ownerNameValidation as Validation.Invalid).reason
            binding.textInputLayoutHolderName.error = localizedContext.getString(errorReasonResId)
        }
        val ibanNumberValidation = outputData.ibanNumberField.validation
        if (!ibanNumberValidation.isValid()) {
            if (!errorFocused) {
                binding.textInputLayoutIbanNumber.requestFocus()
            }
            val errorReasonResId = (ibanNumberValidation as Validation.Invalid).reason
            binding.textInputLayoutIbanNumber.error = localizedContext.getString(errorReasonResId)
        }
    }

    private fun notifyInputDataChanged() {
        sepaDelegate.onInputDataChanged(sepaDelegate.inputData)
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
