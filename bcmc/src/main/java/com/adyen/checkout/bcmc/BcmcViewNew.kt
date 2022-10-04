/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/9/2022.
 */

package com.adyen.checkout.bcmc

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.adyen.checkout.bcmc.databinding.BcmcViewBinding
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.Validation
import kotlinx.coroutines.CoroutineScope

class BcmcViewNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentViewNew {

    private val binding = BcmcViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: BcmcDelegate

    override val isConfirmationRequired: Boolean = true

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is BcmcDelegate) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initCardNumberInput()
        initExpiryDateInput()
        initStorePaymentMethodSwitch()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        with(binding) {
            textInputLayoutCardNumber.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_Card_CardNumberInput,
                localizedContext
            )
            textInputLayoutExpiryDate.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_Card_ExpiryDateInput,
                localizedContext
            )
            switchStorePaymentMethod.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_Card_StorePaymentSwitch,
                localizedContext
            )
        }
    }

    private fun initExpiryDateInput() {
        binding.editTextExpiryDate.setOnChangeListener {
            delegate.inputData.expiryDate = binding.editTextExpiryDate.date
            notifyInputDataChanged()
            binding.textInputLayoutExpiryDate.error = null
        }

        binding.editTextExpiryDate.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val expiryDateValidation = delegate.outputData?.expiryDateField?.validation
            if (hasFocus) {
                binding.textInputLayoutExpiryDate.error = null
            } else if (expiryDateValidation != null && !expiryDateValidation.isValid()) {
                val errorReasonResId = (expiryDateValidation as Validation.Invalid).reason
                binding.textInputLayoutExpiryDate.error = localizedContext.getString(errorReasonResId)
            }
        }
    }

    private fun initCardNumberInput() {
        binding.editTextCardNumber.setOnChangeListener {
            delegate.inputData.cardNumber = binding.editTextCardNumber.rawValue
            notifyInputDataChanged()
            setCardNumberError(null)
        }

        binding.editTextCardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val cardNumberValidation = delegate.outputData?.cardNumberField?.validation
            if (hasFocus) {
                setCardNumberError(null)
            } else if (cardNumberValidation != null && !cardNumberValidation.isValid()) {
                val errorReasonResId = (cardNumberValidation as Validation.Invalid).reason
                setCardNumberError(errorReasonResId)
            }
        }
    }

    private fun initStorePaymentMethodSwitch() {
        binding.switchStorePaymentMethod.isVisible = delegate.configuration.isStorePaymentFieldVisible
        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            delegate.inputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
    }

    private fun notifyInputDataChanged() {
        delegate.onInputDataChanged(delegate.inputData)
    }

    override fun highlightValidationErrors() {
        val outputData = delegate.outputData ?: return

        var isErrorFocused = false
        val cardNumberValidation = outputData.cardNumberField.validation
        if (!cardNumberValidation.isValid()) {
            isErrorFocused = true
            binding.editTextCardNumber.requestFocus()
            val errorReasonResId = (cardNumberValidation as Validation.Invalid).reason
            setCardNumberError(errorReasonResId)
        }
        val expiryFieldValidation = outputData.expiryDateField.validation
        if (!expiryFieldValidation.isValid()) {
            if (!isErrorFocused) {
                binding.textInputLayoutExpiryDate.requestFocus()
            }
            val errorReasonResId = (expiryFieldValidation as Validation.Invalid).reason
            binding.textInputLayoutExpiryDate.error = localizedContext.getString(errorReasonResId)
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?) {
        if (stringResId == null) {
            binding.textInputLayoutCardNumber.error = null
            binding.cardBrandLogoImageView.isVisible = true
        } else {
            binding.textInputLayoutCardNumber.error = localizedContext.getString(stringResId)
            binding.cardBrandLogoImageView.isVisible = false
        }
    }

    override fun getView(): View = this
}
