/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/8/2019.
 */
package com.adyen.checkout.sepa

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.d
import com.adyen.checkout.core.log.Logger.v
import com.adyen.checkout.sepa.databinding.SepaViewBinding

class SepaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<
        SepaOutputData,
        SepaConfiguration,
        PaymentComponentState<SepaPaymentMethod>,
        SepaComponent>(context, attrs, defStyleAttr),
    Observer<SepaOutputData> {

    private val binding: SepaViewBinding = SepaViewBinding.inflate(LayoutInflater.from(getContext()), this)
    private lateinit var holderNameEditText: AdyenTextInputEditText
    private lateinit var ibanNumberEditText: AdyenTextInputEditText
    // Regular View constructor
    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutHolderName.setLocalizedHintFromStyle(R.style.AdyenCheckout_Sepa_HolderNameInput)
        binding.textInputLayoutIbanNumber.setLocalizedHintFromStyle(R.style.AdyenCheckout_Sepa_AccountNumberInput)
    }

    override fun initView() {
        holderNameEditText = binding.textInputLayoutHolderName.editText as AdyenTextInputEditText
        ibanNumberEditText = binding.textInputLayoutIbanNumber.editText as AdyenTextInputEditText

        holderNameEditText.setOnChangeListener {
            component.inputData.name = holderNameEditText.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutHolderName.error = null
        }
        ibanNumberEditText.setOnChangeListener {
            component.inputData.iban = ibanNumberEditText.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutIbanNumber.error = null
        }
        ibanNumberEditText.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            val ibanNumberValidation = outputData?.ibanNumberField?.validation
            if (hasFocus) {
                binding.textInputLayoutIbanNumber.error = null
            } else if (ibanNumberValidation != null && !ibanNumberValidation.isValid()) {
                val errorReasonResId = (ibanNumberValidation as Validation.Invalid).reason
                binding.textInputLayoutIbanNumber.error = localizedContext.getString(errorReasonResId)
            }
        }
    }

    override fun onChanged(sepaOutputData: SepaOutputData?) {
        v(TAG, "sepaOutputData changed")
    }

    override fun onComponentAttached() {
        // nothing to impl
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        d(TAG, "highlightValidationErrors")
        val outputData: SepaOutputData = component.outputData ?: return
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
        component.notifyInputDataChanged()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
