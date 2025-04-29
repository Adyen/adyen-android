/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/9/2022.
 */
package com.adyen.checkout.blik.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.blik.R
import com.adyen.checkout.blik.databinding.BlikViewBinding
import com.adyen.checkout.blik.internal.ui.BlikDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class BlikView @JvmOverloads constructor(
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

    private val binding: BlikViewBinding = BlikViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var blikDelegate: BlikDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is BlikDelegate) { "Unsupported delegate type" }
        blikDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initBlikCodeInput()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutBlikCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Blik_BlikCodeInput,
            localizedContext,
        )
        binding.textViewBlikHeader.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Blik_BlikHeaderTextView,
            localizedContext,
        )
    }

    private fun initBlikCodeInput() {
        binding.editTextBlikCode.apply {
            setOnChangeListener {
                blikDelegate.updateInputData {
                    blikCode = binding.editTextBlikCode.rawValue
                }
                binding.textInputLayoutBlikCode.hideError()
            }

            onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                val outputData = blikDelegate.outputData
                val blikCodeValidation = outputData.blikCodeField.validation
                if (hasFocus) {
                    binding.textInputLayoutBlikCode.hideError()
                } else if (!blikCodeValidation.isValid()) {
                    val errorReasonResId = (blikCodeValidation as Validation.Invalid).reason
                    binding.textInputLayoutBlikCode.showError(localizedContext.getString(errorReasonResId))
                }
            }

            requestFocus()
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val blikCodeValidation = blikDelegate.outputData.blikCodeField.validation
        if (!blikCodeValidation.isValid()) {
            binding.textInputLayoutBlikCode.requestFocus()
            val errorReasonResId = (blikCodeValidation as Validation.Invalid).reason
            binding.textInputLayoutBlikCode.showError(localizedContext.getString(errorReasonResId))
        }
    }

    override fun getView(): View = this
}
