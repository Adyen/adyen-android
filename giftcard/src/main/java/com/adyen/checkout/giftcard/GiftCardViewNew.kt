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
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.giftcard.databinding.GiftcardViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val TAG = LogUtil.getTag()

class GiftCardViewNew @JvmOverloads constructor(
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

    private val binding: GiftcardViewBinding = GiftcardViewBinding.inflate(LayoutInflater.from(context), this)

    private var imageLoader: ImageLoader? = null
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

        imageLoader = ImageLoader.getInstance(context, delegate.configuration.environment)

        observeDelegate(delegate, coroutineScope)
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

    private fun observeDelegate(delegate: GiftCardDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(giftCardOutputData: GiftCardOutputData?) {
        Logger.v(TAG, "GiftCardOutputData changed")
        // no ops
    }

    private fun initInputs() {
        binding.editTextGiftcardNumber.setOnChangeListener {
            giftCardDelegate.inputData.cardNumber = binding.editTextGiftcardNumber.rawValue
            notifyInputDataChanged()
            binding.textInputLayoutGiftcardNumber.error = null
        }

        binding.editTextGiftcardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val cardNumberValidation = giftCardDelegate.outputData?.giftcardNumberFieldState?.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardNumber.error = null
            } else if (cardNumberValidation != null && cardNumberValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardNumber.error = localizedContext.getString(cardNumberValidation.reason)
            }
        }

        binding.editTextGiftcardPin.setOnChangeListener { editable: Editable ->
            giftCardDelegate.inputData.pin = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutGiftcardPin.error = null
        }

        binding.editTextGiftcardPin.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val pinValidation = giftCardDelegate.outputData?.giftcardPinFieldState?.validation
            if (hasFocus) {
                binding.textInputLayoutGiftcardPin.error = null
            } else if (pinValidation != null && pinValidation is Validation.Invalid) {
                binding.textInputLayoutGiftcardPin.error = localizedContext.getString(pinValidation.reason)
            }
        }
    }

    override val isConfirmationRequired: Boolean = true

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val outputData = giftCardDelegate.outputData ?: return
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
        giftCardDelegate.onInputDataChanged(giftCardDelegate.inputData)
    }

    override fun getView(): View = this
}
