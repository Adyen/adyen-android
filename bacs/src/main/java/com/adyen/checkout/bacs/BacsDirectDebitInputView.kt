/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.bacs.databinding.BacsDirectDebitInputViewBinding
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class BacsDirectDebitInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<BacsDirectDebitOutputData,
        BacsDirectDebitConfiguration,
        BacsDirectDebitComponentState,
        BacsDirectDebitComponent>(context, attrs, defStyleAttr),
    Observer<BacsDirectDebitOutputData> {

    private val binding: BacsDirectDebitInputViewBinding =
        BacsDirectDebitInputViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onComponentAttached() {
        component.outputData?.let {
            updateInputData(it)

            binding.editTextHolderName.setText(it.holderNameState.value)
            binding.editTextBankAccountNumber.setText(it.bankAccountNumberState.value)
            binding.editTextSortCode.setText(it.sortCodeState.value)
            binding.editTextShopperEmail.setText(it.shopperEmailState.value)
            binding.switchConsentAmount.isChecked = it.isAmountConsentChecked
            binding.switchConsentAccount.isChecked = it.isAccountConsentChecked
        }
        component.setInputMode()
    }

    override fun initView() {
        initHolderNameInput()
        initBankAccountNumberInput()
        initSortCodeInput()
        initShopperEmailInput()
        initConsentSwitches()
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        component.outputData?.let {
            var isErrorFocused = false
            val holderNameValidation = it.holderNameState.validation
            if (holderNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextHolderName.requestFocus()
                binding.textInputLayoutHolderName.error = localizedContext.getString(holderNameValidation.reason)
            }
            val bankAccountNumberValidation = it.bankAccountNumberState.validation
            if (bankAccountNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextBankAccountNumber.requestFocus()
                }
                binding.textInputLayoutBankAccountNumber.error =
                    localizedContext.getString(bankAccountNumberValidation.reason)
            }
            val sortCodeValidation = it.sortCodeState.validation
            if (sortCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextSortCode.requestFocus()
                }
                binding.textInputLayoutSortCode.error = localizedContext.getString(sortCodeValidation.reason)
            }
            val shopperEmailValidation = it.shopperEmailState.validation
            if (shopperEmailValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    binding.editTextShopperEmail.requestFocus()
                }
                binding.textInputLayoutShopperEmail.error = localizedContext.getString(shopperEmailValidation.reason)
            }
            if (!it.isAmountConsentChecked) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.switchConsentAmount.requestFocus()
                }
                binding.textViewErrorConsentAmount.isVisible = true
            }
            if (!it.isAccountConsentChecked) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.switchConsentAccount.requestFocus()
                }
                binding.textViewErrorConsentAccount.isVisible = true
            }
        }
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutHolderName.setLocalizedHintFromStyle(R.style.AdyenCheckout_Bacs_HolderNameInput)
        binding.textInputLayoutBankAccountNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_AccountNumberInput
        )
        binding.textInputLayoutSortCode.setLocalizedHintFromStyle(R.style.AdyenCheckout_Bacs_SortCodeInput)
        binding.textInputLayoutShopperEmail.setLocalizedHintFromStyle(R.style.AdyenCheckout_Bacs_ShopperEmailInput)
        binding.switchConsentAccount.setLocalizedTextFromStyle(R.style.AdyenCheckout_Bacs_Switch_Account)
        setAmountConsentSwitchText()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(bacsDirectDebitOutputData: BacsDirectDebitOutputData) {
        Logger.v(TAG, "bacsDirectDebitOutputData changed")
        onBankAccountNumberValidated(bacsDirectDebitOutputData.bankAccountNumberState)
        onSortCodeValidated(bacsDirectDebitOutputData.sortCodeState)
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }

    private fun initHolderNameInput() {
        val holderNameEditText = binding.editTextHolderName as? AdyenTextInputEditText
        holderNameEditText?.setOnChangeListener {
            component.inputData.holderName = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutHolderName.error = null
        }
        holderNameEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = component.outputData?.holderNameState?.validation
            if (hasFocus) {
                binding.textInputLayoutHolderName.error = null
            } else if (holderNameValidation != null && holderNameValidation is Validation.Invalid) {
                binding.textInputLayoutHolderName.error = localizedContext.getString(holderNameValidation.reason)
            }
        }
    }

    private fun initBankAccountNumberInput() {
        val bankAccountNumberEditText = binding.editTextBankAccountNumber as? AdyenTextInputEditText
        bankAccountNumberEditText?.setOnChangeListener {
            component.inputData.bankAccountNumber = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutBankAccountNumber.error = null
        }
        bankAccountNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val bankAccountNumberValidation = component.outputData?.bankAccountNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutBankAccountNumber.error = null
            } else if (bankAccountNumberValidation != null && bankAccountNumberValidation is Validation.Invalid) {
                binding.textInputLayoutBankAccountNumber.error =
                    localizedContext.getString(bankAccountNumberValidation.reason)
            }
        }
    }

    private fun initSortCodeInput() {
        val sortCodeEditText = binding.editTextSortCode as? AdyenTextInputEditText
        sortCodeEditText?.setOnChangeListener {
            component.inputData.sortCode = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSortCode.error = null
        }
        sortCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val sortCodeValidation = component.outputData?.sortCodeState?.validation
            if (hasFocus) {
                binding.textInputLayoutSortCode.error = null
            } else if (sortCodeValidation != null && sortCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSortCode.error = localizedContext.getString(sortCodeValidation.reason)
            }
        }
    }

    private fun initShopperEmailInput() {
        val shopperEmailEditText = binding.editTextShopperEmail as? AdyenTextInputEditText
        shopperEmailEditText?.setOnChangeListener {
            component.inputData.shopperEmail = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutShopperEmail.error = null
        }
        shopperEmailEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val shopperEmailValidation = component.outputData?.shopperEmailState?.validation
            if (hasFocus) {
                binding.textInputLayoutShopperEmail.error = null
            } else if (shopperEmailValidation != null && shopperEmailValidation is Validation.Invalid) {
                binding.textInputLayoutShopperEmail.error = localizedContext.getString(shopperEmailValidation.reason)
            }
        }
    }

    private fun initConsentSwitches() {
        binding.switchConsentAmount.setOnCheckedChangeListener { _, isChecked ->
            component.inputData.isAmountConsentChecked = isChecked
            binding.textViewErrorConsentAmount.isVisible = !isChecked
            notifyInputDataChanged()
        }

        binding.switchConsentAccount.setOnCheckedChangeListener { _, isChecked ->
            component.inputData.isAccountConsentChecked = isChecked
            binding.textViewErrorConsentAccount.isVisible = !isChecked
            notifyInputDataChanged()
        }
    }

    private fun setAmountConsentSwitchText() {
        if (!component.configuration.amount.isEmpty) {
            val formattedAmount =
                CurrencyUtils.formatAmount(component.configuration.amount, component.configuration.shopperLocale)
            binding.switchConsentAmount.text =
                localizedContext.getString(R.string.bacs_consent_amount_specified, formattedAmount)
        } else {
            binding.switchConsentAmount.setLocalizedTextFromStyle(R.style.AdyenCheckout_Bacs_Switch_Amount)
        }
    }

    private fun updateInputData(outputData: BacsDirectDebitOutputData) {
        component.inputData.apply {
            holderName = outputData.holderNameState.value
            bankAccountNumber = outputData.bankAccountNumberState.value
            sortCode = outputData.sortCodeState.value
            shopperEmail = outputData.shopperEmailState.value
            isAccountConsentChecked = outputData.isAccountConsentChecked
            isAmountConsentChecked = outputData.isAmountConsentChecked
        }
    }

    private fun onBankAccountNumberValidated(bankAccountNumberFieldState: FieldState<String>) {
        if (bankAccountNumberFieldState.validation.isValid()) {
            goToNextInputIfFocus(binding.editTextBankAccountNumber)
        }
    }

    private fun onSortCodeValidated(sortCodeFieldState: FieldState<String>) {
        if (sortCodeFieldState.validation.isValid()) {
            goToNextInputIfFocus(binding.editTextSortCode)
        }
    }

    private fun goToNextInputIfFocus(view: View?) {
        if (rootView.findFocus() === view && view != null) {
            findViewById<View>(view.nextFocusForwardId).requestFocus()
        }
    }
}
