/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 29/3/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.StoredCardViewBinding
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.internal.util.BuildUtils
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo
import com.adyen.checkout.ui.core.old.internal.ui.view.RoundCornerImageView
import com.adyen.checkout.ui.core.old.internal.ui.view.SecurityCodeInput
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.old.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

/**
 * StoredCardView for [CardComponent].
 */

@Suppress("TooManyFunctions")
internal class StoredCardView @JvmOverloads constructor(
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

    private val binding: StoredCardViewBinding = StoredCardViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var cardDelegate: CardDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!BuildUtils.isDebugBuild(context)) {
            // Prevent taking screenshot and screen on recents.
            getActivity(context)?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!BuildUtils.isDebugBuild(context)) {
            getActivity(context)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is CardDelegate) { "Unsupported delegate type" }
        cardDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)
        initSecurityCodeInput()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutCardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_CardNumberInput,
            localizedContext
        )
        binding.textInputLayoutExpiryDate.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_ExpiryDateInput,
            localizedContext
        )
        binding.textInputLayoutSecurityCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_SecurityCodeInput,
            localizedContext
        )
    }

    private fun observeDelegate(delegate: CardDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun initSecurityCodeInput() {
        val securityCodeEditText = binding.textInputLayoutSecurityCode.editText as? SecurityCodeInput
        securityCodeEditText?.setOnChangeListener { editable: Editable ->
            cardDelegate.updateInputData { securityCode = editable.toString() }
            binding.textInputLayoutSecurityCode.hideError()
        }
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val securityCodeValidation = cardDelegate.outputData.securityCodeState.validation
            if (hasFocus) {
                binding.textInputLayoutSecurityCode.hideError()
            } else if (securityCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSecurityCode.showError(localizedContext.getString(securityCodeValidation.reason))
            }
        }
        binding.textInputLayoutSecurityCode.takeIf { isVisible }?.requestFocus()
    }

    private fun outputDataChanged(cardOutputData: CardOutputData) {
        binding.editTextCardNumber.setText(
            localizedContext.getString(
                R.string.card_number_4digit,
                cardOutputData.cardNumberState.value
            )
        )
        binding.editTextExpiryDate.date = cardOutputData.expiryDateState.value
        setDetectedCardBrand(cardOutputData)
    }

    private fun setDetectedCardBrand(cardOutputData: CardOutputData) {
        val detectedCardTypes = cardOutputData.detectedCardTypes
        if (detectedCardTypes.isNotEmpty()) {
            binding.cardBrandLogoImageViewPrimary.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
            binding.cardBrandLogoImageViewPrimary.loadLogo(
                environment = cardDelegate.componentParams.environment,
                txVariant = detectedCardTypes[0].cardBrand.txVariant,
                placeholder = R.drawable.ic_card,
                errorFallback = R.drawable.ic_card,
            )
        }
    }

    override fun highlightValidationErrors() {
        cardDelegate.outputData.let {
            val securityCodeValidation = it.securityCodeState.validation
            if (securityCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSecurityCode.requestFocus()
                binding.textInputLayoutSecurityCode.showError(localizedContext.getString(securityCodeValidation.reason))
            }
        }
    }

    override fun getView(): View = this

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }
}
