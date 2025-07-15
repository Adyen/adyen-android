/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */
package com.adyen.checkout.giftcard.internal.ui.view

import android.content.Context
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.autofill.HintConstants
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.giftcard.R
import com.adyen.checkout.giftcard.databinding.GiftcardViewBinding
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.isVisible
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.old.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class GiftCardView @JvmOverloads constructor(
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

    private val binding: GiftcardViewBinding = GiftcardViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var giftCardDelegate: GiftCardDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.editTextGiftcardPin.setAutofillHints(HintConstants.AUTOFILL_HINT_GIFT_CARD_PIN)
        }
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is GiftCardDelegate) { "Unsupported delegate type" }
        giftCardDelegate = delegate

        this.localizedContext = localizedContext
        initCardNumberField(localizedContext)
        initPinField(localizedContext)
    }

    private fun initCardNumberField(localizedContext: Context) {
        binding.textInputLayoutGiftcardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_GiftCard_GiftCardNumberInput,
            localizedContext,
        )

        binding.editTextGiftcardNumber.setOnChangeListener {
            giftCardDelegate.updateInputData { cardNumber = binding.editTextGiftcardNumber.rawValue }
            binding.textInputLayoutGiftcardNumber.hideError()
        }

        binding.editTextGiftcardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val cardNumberValidation = giftCardDelegate.outputData.numberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardNumber.hideError()
            } else if (cardNumberValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardNumber.showError(localizedContext.getString(cardNumberValidation.reason))
            }
        }
    }

    private fun initPinField(localizedContext: Context) {
        if (giftCardDelegate.isPinRequired()) {
            binding.textInputLayoutGiftcardPin.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_GiftCard_GiftCardPinInput,
                localizedContext,
            )

            binding.editTextGiftcardPin.setOnChangeListener { editable: Editable ->
                giftCardDelegate.updateInputData { pin = editable.toString() }
                binding.textInputLayoutGiftcardPin.hideError()
            }

            binding.editTextGiftcardPin.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val pinValidation = giftCardDelegate.outputData.pinFieldState.validation
                if (hasFocus) {
                    binding.textInputLayoutGiftcardPin.hideError()
                } else if (pinValidation is Validation.Invalid) {
                    binding.textInputLayoutGiftcardPin.showError(localizedContext.getString(pinValidation.reason))
                }
            }
        } else {
            binding.textInputLayoutGiftcardPin.isVisible = false
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val outputData = giftCardDelegate.outputData
        var isErrorFocused = false
        val cardNumberValidation = outputData.numberFieldState.validation
        if (cardNumberValidation is Validation.Invalid) {
            isErrorFocused = true
            binding.textInputLayoutGiftcardNumber.requestFocus()
            binding.textInputLayoutGiftcardNumber.showError(localizedContext.getString(cardNumberValidation.reason))
        }
        val pinValidation = outputData.pinFieldState.validation
        if (pinValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutGiftcardPin.requestFocus()
            }
            binding.textInputLayoutGiftcardPin.showError(localizedContext.getString(pinValidation.reason))
        }
    }

    override fun getView(): View = this
}
