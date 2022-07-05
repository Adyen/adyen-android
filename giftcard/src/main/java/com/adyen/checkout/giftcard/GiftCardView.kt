/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */

package com.adyen.checkout.giftcard

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.giftcard.databinding.GiftcardViewBinding

private val TAG = LogUtil.getTag()

class GiftCardView :
    AdyenLinearLayout<GiftCardOutputData, GiftCardConfiguration, GiftCardComponentState, GiftCardComponent>,
    Observer<GiftCardOutputData> {

    private val binding: GiftcardViewBinding = GiftcardViewBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutGiftcardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_GiftCard_GiftCardNumberInput
        )
        binding.textInputLayoutGiftcardPin.setLocalizedHintFromStyle(R.style.AdyenCheckout_GiftCard_GiftCardPinInput)
    }

    override fun initView() {
        binding.editTextGiftcardNumber.setOnChangeListener {
            component.inputData.cardNumber = binding.editTextGiftcardNumber.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutGiftcardNumber.error = null
        }

        binding.editTextGiftcardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val cardNumberValidation = component.outputData?.giftcardNumberFieldState?.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardNumber.error = null
            } else if (cardNumberValidation != null && cardNumberValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardNumber.error = localizedContext.getString(cardNumberValidation.reason)
            }
        }

        binding.editTextGiftcardPin.setOnChangeListener { editable: Editable ->
            component.inputData.pin = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutGiftcardPin.error = null
        }

        binding.editTextGiftcardPin.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val pinValidation = component.outputData?.giftcardPinFieldState?.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardPin.error = null
            } else if (pinValidation != null && pinValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardPin.error = localizedContext.getString(pinValidation.reason)
            }
        }

        notifyInputDataChanged()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onComponentAttached() {
        // nothing to impl
    }

    override fun onChanged(giftCardOutputData: GiftCardOutputData?) {
        Logger.v(TAG, "GiftCardOutputData changed")
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData = component.outputData ?: return
        var isErrorFocused = false
        val cardNumberValidation = outputData.giftcardNumberFieldState.validation
        if (cardNumberValidation is Validation.Invalid) {
            isErrorFocused = true
            binding.textInputLayoutGiftcardNumber.requestFocus()
            binding.textInputLayoutGiftcardNumber.error = localizedContext.getString(cardNumberValidation.reason)
        }
        val pinValidation = outputData.giftcardPinFieldState.validation
        if (pinValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutGiftcardPin.requestFocus()
            }
            binding.textInputLayoutGiftcardPin.error = localizedContext.getString(pinValidation.reason)
        }
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }
}
