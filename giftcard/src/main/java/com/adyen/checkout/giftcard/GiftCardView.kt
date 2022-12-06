/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */
package com.adyen.checkout.giftcard

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.hideError
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.showError
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.giftcard.databinding.GiftcardViewBinding
import kotlinx.coroutines.CoroutineScope

internal class GiftCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: GiftcardViewBinding = GiftcardViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var giftCardDelegate: GiftCardDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is GiftCardDelegate) throw IllegalArgumentException("Unsupported delegate type")
        giftCardDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initInputs()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutGiftcardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_GiftCard_GiftCardNumberInput,
            localizedContext
        )
        binding.textInputLayoutGiftcardPin.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_GiftCard_GiftCardPinInput,
            localizedContext
        )
    }

    private fun initInputs() {
        binding.editTextGiftcardNumber.setOnChangeListener {
            giftCardDelegate.updateInputData { cardNumber = binding.editTextGiftcardNumber.rawValue }
            binding.textInputLayoutGiftcardNumber.hideError()
        }

        binding.editTextGiftcardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val cardNumberValidation = giftCardDelegate.outputData.giftcardNumberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardNumber.hideError()
            } else if (cardNumberValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardNumber.showError(localizedContext.getString(cardNumberValidation.reason))
            }
        }

        binding.editTextGiftcardPin.setOnChangeListener { editable: Editable ->
            giftCardDelegate.updateInputData { pin = editable.toString() }
            binding.textInputLayoutGiftcardPin.hideError()
        }

        binding.editTextGiftcardPin.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val pinValidation = giftCardDelegate.outputData.giftcardPinFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardPin.hideError()
            } else if (pinValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardPin.showError(localizedContext.getString(pinValidation.reason))
            }
        }
    }

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData = giftCardDelegate.outputData
        var isErrorFocused = false
        val cardNumberValidation = outputData.giftcardNumberFieldState.validation
        if (cardNumberValidation is Validation.Invalid) {
            isErrorFocused = true
            binding.textInputLayoutGiftcardNumber.requestFocus()
            binding.textInputLayoutGiftcardNumber.showError(localizedContext.getString(cardNumberValidation.reason))
        }
        val pinValidation = outputData.giftcardPinFieldState.validation
        if (pinValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutGiftcardPin.requestFocus()
            }
            binding.textInputLayoutGiftcardPin.showError(localizedContext.getString(pinValidation.reason))
        }
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
