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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.bacs.databinding.BacsDirectDebitViewBinding
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class BacsDirectDebitView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenLinearLayout<BacsDirectDebitOutputData,
        BacsDirectDebitConfiguration,
        BacsDirectDebitComponentState,
        BacsDirectDebitComponent>(context, attrs, defStyleAttr),
    Observer<BacsDirectDebitOutputData> {

    private val binding: BacsDirectDebitViewBinding = BacsDirectDebitViewBinding.inflate(LayoutInflater.from(context), this)

    private val mBacsDirectDebitInputData = BacsDirectDebitInputData()

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onComponentAttached() {
        // no ops
    }

    override fun initView() {
        initHolderNameInput()
        initBankAccountNumberInput()
        initSortCodeInput()
        initShopperEmailInput()
        initConsentSwitches()
    }

    override fun isConfirmationRequired(): Boolean {
        return true
    }

    override fun highlightValidationErrors() {
        component.outputData?.let {
            var isErrorFocused = false
            val holderNameValidation = it.holderNameState.validation
            if (holderNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextHolderName.requestFocus()
                binding.textInputLayoutHolderName.error = mLocalizedContext.getString(holderNameValidation.reason)
            }
            val bankAccountNumberValidation = it.bankAccountNumberState.validation
            if (bankAccountNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextBankAccountNumber.requestFocus()
                }
                binding.textInputLayoutBankAccountNumber.error = mLocalizedContext.getString(bankAccountNumberValidation.reason)
            }
            val sortCodeValidation = it.sortCodeState.validation
            if (sortCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextSortCode.requestFocus()
                }
                binding.textInputLayoutSortCode.error = mLocalizedContext.getString(sortCodeValidation.reason)
            }
            val shopperEmailValidation = it.shopperEmailState.validation
            if (shopperEmailValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextShopperEmail.requestFocus()
                }
                binding.textInputLayoutShopperEmail.error = mLocalizedContext.getString(shopperEmailValidation.reason)
            }
        }
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        val attrs = intArrayOf(android.R.attr.hint)

        // Holder Name
        var typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Bacs_HolderNameInput, attrs)
        binding.textInputLayoutHolderName.hint = typedArray.getString(0)
        typedArray.recycle()

        // Account Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Bacs_AccountNumberInput, attrs)
        binding.textInputLayoutBankAccountNumber.hint = typedArray.getString(0)
        typedArray.recycle()

        // Sort Code
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Bacs_SortCodeInput, attrs)
        binding.textInputLayoutSortCode.hint = typedArray.getString(0)
        typedArray.recycle()

        // Shopper Email
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Bacs_ShopperEmailInput, attrs)
        binding.textInputLayoutShopperEmail.hint = typedArray.getString(0)
        typedArray.recycle()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(bacsDirectDebitOutputData: BacsDirectDebitOutputData) {
        Logger.v(TAG, "bacsDirectDebitOutputData changed")
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(mBacsDirectDebitInputData)
    }

    private fun initHolderNameInput() {
        val holderNameEditText = binding.editTextHolderName as? AdyenTextInputEditText
        holderNameEditText?.setOnChangeListener {
            mBacsDirectDebitInputData.holderName = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutHolderName.error = null
        }
        holderNameEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = component.outputData?.holderNameState?.validation
            if (hasFocus) {
                binding.textInputLayoutHolderName.error = null
            } else if (holderNameValidation != null && holderNameValidation is Validation.Invalid) {
                binding.textInputLayoutHolderName.error = mLocalizedContext.getString(holderNameValidation.reason)
            }
        }
    }

    private fun initBankAccountNumberInput() {
        val bankAccountNumberEditText = binding.editTextBankAccountNumber as? AdyenTextInputEditText
        bankAccountNumberEditText?.setOnChangeListener {
            mBacsDirectDebitInputData.bankAccountNumber = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutBankAccountNumber.error = null
        }
        bankAccountNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val bankAccountNumberValidation = component.outputData?.bankAccountNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutBankAccountNumber.error = null
            } else if (bankAccountNumberValidation != null && bankAccountNumberValidation is Validation.Invalid) {
                binding.textInputLayoutBankAccountNumber.error = mLocalizedContext.getString(bankAccountNumberValidation.reason)
            }
        }
    }

    private fun initSortCodeInput() {
        val sortCodeEditText = binding.editTextSortCode as? AdyenTextInputEditText
        sortCodeEditText?.setOnChangeListener {
            mBacsDirectDebitInputData.sortCode = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSortCode.error = null
        }
        sortCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val sortCodeValidation = component.outputData?.sortCodeState?.validation
            if (hasFocus) {
                binding.textInputLayoutSortCode.error = null
            } else if (sortCodeValidation != null && sortCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSortCode.error = mLocalizedContext.getString(sortCodeValidation.reason)
            }
        }
    }

    private fun initShopperEmailInput() {
        val shopperEmailEditText = binding.editTextShopperEmail as? AdyenTextInputEditText
        shopperEmailEditText?.setOnChangeListener {
            mBacsDirectDebitInputData.shopperEmail = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutShopperEmail.error = null
        }
        shopperEmailEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val shopperEmailValidation = component.outputData?.shopperEmailState?.validation
            if (hasFocus) {
                binding.textInputLayoutShopperEmail.error = null
            } else if (shopperEmailValidation != null && shopperEmailValidation is Validation.Invalid) {
                binding.textInputLayoutShopperEmail.error = mLocalizedContext.getString(shopperEmailValidation.reason)
            }
        }
    }

    private fun initConsentSwitches() {
        binding.switchConsentAmount.setOnCheckedChangeListener { _, isChecked ->
            mBacsDirectDebitInputData.isAmountConsentChecked = isChecked
            notifyInputDataChanged()
        }

        binding.switchConsentAccount.setOnCheckedChangeListener { _, isChecked ->
            mBacsDirectDebitInputData.isAccountConsentChecked = isChecked
            notifyInputDataChanged()
        }
    }
}
