/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.mealvoucher.R
import com.adyen.checkout.mealvoucher.databinding.MealVoucherViewBinding
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.isVisible
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class MealVoucherView @JvmOverloads constructor(
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

    private val binding: MealVoucherViewBinding = MealVoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var giftCardDelegate: GiftCardDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)

        // TODO Support autofill if necessary
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
        binding.textInputLayoutMealVoucherCardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_MealVoucher_CardNumberInput,
            localizedContext,
        )

        binding.editTextMealVoucherCardNumber.setOnChangeListener {
            giftCardDelegate.updateInputData { cardNumber = binding.editTextMealVoucherCardNumber.rawValue }
            binding.textInputLayoutMealVoucherCardNumber.hideError()
        }

        binding.editTextMealVoucherCardNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val cardNumberValidation = giftCardDelegate.outputData.numberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutMealVoucherCardNumber.hideError()
            } else if (cardNumberValidation is Validation.Invalid) {
                binding.textInputLayoutMealVoucherCardNumber.showError(
                    localizedContext.getString(cardNumberValidation.reason)
                )
            }
        }
    }

    private fun initExpiryDateField(localizedContext: Context) {
        binding.textInputLayoutMealVoucherExpiryDate.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_MealVoucher_ExpiryDateInput,
            localizedContext,
        )

        binding.editTextMealVoucherExpiryDate.setOnChangeListener {
            val date = binding.editTextMealVoucherExpiryDate.date
            giftCardDelegate.updateInputData {
                expiryDate = date
            }
            binding.textInputLayoutMealVoucherExpiryDate.hideError()
        }

        binding.editTextMealVoucherExpiryDate.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val expiryDateValidation = giftCardDelegate.outputData.expiryDateFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutMealVoucherExpiryDate.hideError()
            } else if (expiryDateValidation is Validation.Invalid) {
                binding.textInputLayoutMealVoucherExpiryDate.showError(
                    localizedContext.getString(
                        expiryDateValidation.reason,
                    ),
                )
            }
        }
    }

    private fun initSecurityCodeField(localizedContext: Context) {
        if (giftCardDelegate.isPinRequired()) {
            binding.textInputLayoutMealVoucherSecurityCode.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_MealVoucher_SecurityCodeInput,
                localizedContext,
            )

            binding.editTextMealVoucherSecurityCode.setOnChangeListener { editable: Editable ->
                giftCardDelegate.updateInputData { pin = editable.toString() }
                binding.textInputLayoutMealVoucherSecurityCode.hideError()
            }

            binding.editTextMealVoucherSecurityCode.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val securityCodeValidation = giftCardDelegate.outputData.pinFieldState.validation
                if (hasFocus) {
                    binding.textInputLayoutMealVoucherSecurityCode.hideError()
                } else if (securityCodeValidation is Validation.Invalid) {
                    binding.textInputLayoutMealVoucherSecurityCode.showError(
                        localizedContext.getString(
                            securityCodeValidation.reason,
                        ),
                    )
                }
            }
        } else {
            binding.textInputLayoutMealVoucherSecurityCode.isVisible = false
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        val outputData = giftCardDelegate.outputData
        var isErrorFocused = false

        val cardNumberValidation = outputData.numberFieldState.validation
        if (cardNumberValidation is Validation.Invalid) {
            isErrorFocused = true
            binding.textInputLayoutMealVoucherCardNumber.requestFocus()
            binding.textInputLayoutMealVoucherCardNumber.showError(
                localizedContext.getString(cardNumberValidation.reason),
            )
        }

        val expiryDateValidation = outputData.expiryDateFieldState.validation
        if (expiryDateValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutMealVoucherExpiryDate.requestFocus()
            }
            binding.textInputLayoutMealVoucherExpiryDate.showError(
                localizedContext.getString(expiryDateValidation.reason),
            )
        }

        val securityCodeValidation = outputData.pinFieldState.validation
        if (securityCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                binding.textInputLayoutMealVoucherSecurityCode.requestFocus()
            }
            binding.textInputLayoutMealVoucherSecurityCode.showError(
                localizedContext.getString(securityCodeValidation.reason),
            )
        }
    }

    override fun getView(): View = this
}
