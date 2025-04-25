/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.ui.view

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
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.mealvoucherfr.R
import com.adyen.checkout.mealvoucherfr.databinding.MealVoucherFrViewBinding
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.isVisible
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class MealVoucherFRView @JvmOverloads constructor(
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

    private val binding: MealVoucherFrViewBinding = MealVoucherFrViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var giftCardDelegate: GiftCardDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.editTextMealVoucherFRSecurityCode.setAutofillHints(HintConstants.AUTOFILL_HINT_GIFT_CARD_PIN)
        }
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is GiftCardDelegate) { "Unsupported delegate type" }
        giftCardDelegate = delegate

        this.localizedContext = localizedContext
        initCardNumberField(localizedContext)
        initExpiryDateField(localizedContext)
        initSecurityCodeField(localizedContext)
    }

    private fun initCardNumberField(localizedContext: Context) {
        binding.textInputLayoutMealVoucherFRCardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_MealVoucherFR_CardNumberInput,
            localizedContext,
        )

        binding.editTextMealVoucherFRCardNumber.apply {
            setOnChangeListener {
                giftCardDelegate.updateInputData { cardNumber = binding.editTextMealVoucherFRCardNumber.rawValue }
                binding.textInputLayoutMealVoucherFRCardNumber.hideError()
            }

            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val cardNumberValidation = giftCardDelegate.outputData.numberFieldState.validation
                if (hasFocus) {
                    binding.textInputLayoutMealVoucherFRCardNumber.hideError()
                } else if (cardNumberValidation is Validation.Invalid) {
                    binding.textInputLayoutMealVoucherFRCardNumber.showError(
                        localizedContext.getString(cardNumberValidation.reason),
                    )
                }
            }

            requestFocus()
        }
    }

    private fun initExpiryDateField(localizedContext: Context) {
        binding.textInputLayoutMealVoucherFRExpiryDate.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_MealVoucherFR_ExpiryDateInput,
            localizedContext,
        )

        binding.editTextMealVoucherFRExpiryDate.setOnChangeListener {
            val date = binding.editTextMealVoucherFRExpiryDate.date
            giftCardDelegate.updateInputData {
                expiryDate = date
            }
            binding.textInputLayoutMealVoucherFRExpiryDate.hideError()
        }

        binding.editTextMealVoucherFRExpiryDate.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val expiryDateValidation = giftCardDelegate.outputData.expiryDateFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutMealVoucherFRExpiryDate.hideError()
            } else if (expiryDateValidation is Validation.Invalid) {
                binding.textInputLayoutMealVoucherFRExpiryDate.showError(
                    localizedContext.getString(
                        expiryDateValidation.reason,
                    ),
                )
            }
        }
    }

    private fun initSecurityCodeField(localizedContext: Context) {
        if (giftCardDelegate.isPinRequired()) {
            binding.textInputLayoutMealVoucherFRSecurityCode.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_MealVoucherFR_SecurityCodeInput,
                localizedContext,
            )

            binding.editTextMealVoucherFRSecurityCode.setOnChangeListener { editable: Editable ->
                giftCardDelegate.updateInputData { pin = editable.toString() }
                binding.textInputLayoutMealVoucherFRSecurityCode.hideError()
            }

            binding.editTextMealVoucherFRSecurityCode.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val securityCodeValidation = giftCardDelegate.outputData.pinFieldState.validation
                if (hasFocus) {
                    binding.textInputLayoutMealVoucherFRSecurityCode.hideError()
                } else if (securityCodeValidation is Validation.Invalid) {
                    binding.textInputLayoutMealVoucherFRSecurityCode.showError(
                        localizedContext.getString(
                            securityCodeValidation.reason,
                        ),
                    )
                }
            }
        } else {
            binding.textInputLayoutMealVoucherFRSecurityCode.isVisible = false
            (binding.textInputLayoutMealVoucherFRExpiryDate.layoutParams as LayoutParams).marginEnd = 0
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val outputData = giftCardDelegate.outputData
        var isErrorFocused = false

        val cardNumberValidation = outputData.numberFieldState.validation
        if (cardNumberValidation is Validation.Invalid) {
            isErrorFocused = true
            binding.textInputLayoutMealVoucherFRCardNumber.requestFocus()
            binding.textInputLayoutMealVoucherFRCardNumber.showError(
                localizedContext.getString(cardNumberValidation.reason),
            )
        }

        val expiryDateValidation = outputData.expiryDateFieldState.validation
        if (expiryDateValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutMealVoucherFRExpiryDate.requestFocus()
            }
            binding.textInputLayoutMealVoucherFRExpiryDate.showError(
                localizedContext.getString(expiryDateValidation.reason),
            )
        }

        val securityCodeValidation = outputData.pinFieldState.validation
        if (securityCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutMealVoucherFRSecurityCode.requestFocus()
            }
            binding.textInputLayoutMealVoucherFRSecurityCode.showError(
                localizedContext.getString(securityCodeValidation.reason),
            )
        }
    }

    override fun getView(): View = this
}
