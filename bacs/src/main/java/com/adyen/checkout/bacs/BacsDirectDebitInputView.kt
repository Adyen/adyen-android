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
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.databinding.BacsDirectDebitInputViewBinding
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class BacsDirectDebitInputView @JvmOverloads constructor(
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

    private val binding: BacsDirectDebitInputViewBinding =
        BacsDirectDebitInputViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var bacsDelegate: BacsDirectDebitDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is BacsDirectDebitDelegate) throw IllegalArgumentException("Unsupported delegate type")
        bacsDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        bacsDelegate.outputData?.let {
            updateInputData(it)

            binding.editTextHolderName.setText(it.holderNameState.value)
            binding.editTextBankAccountNumber.setText(it.bankAccountNumberState.value)
            binding.editTextSortCode.setText(it.sortCodeState.value)
            binding.editTextShopperEmail.setText(it.shopperEmailState.value)
            binding.switchConsentAmount.isChecked = it.isAmountConsentChecked
            binding.switchConsentAccount.isChecked = it.isAccountConsentChecked
        }

        initHolderNameInput()
        initBankAccountNumberInput()
        initSortCodeInput()
        initShopperEmailInput()
        initConsentSwitches()
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        bacsDelegate.outputData?.let {
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

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutHolderName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_HolderNameInput,
            localizedContext
        )
        binding.textInputLayoutBankAccountNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_AccountNumberInput,
            localizedContext
        )
        binding.textInputLayoutSortCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_SortCodeInput,
            localizedContext
        )
        binding.textInputLayoutShopperEmail.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_ShopperEmailInput,
            localizedContext
        )
        binding.switchConsentAccount.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Bacs_Switch_Account,
            localizedContext
        )
        setAmountConsentSwitchText()
    }

    private fun observeDelegate(delegate: BacsDirectDebitDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(bacsDirectDebitOutputData: BacsDirectDebitOutputData?) {
        Logger.v(TAG, "bacsDirectDebitOutputData changed")
        bacsDirectDebitOutputData ?: return

        onBankAccountNumberValidated(bacsDirectDebitOutputData.bankAccountNumberState)
        onSortCodeValidated(bacsDirectDebitOutputData.sortCodeState)
    }

    private fun notifyInputDataChanged() {
        bacsDelegate.onInputDataChanged(bacsDelegate.inputData)
    }

    private fun initHolderNameInput() {
        val holderNameEditText = binding.editTextHolderName as? AdyenTextInputEditText
        holderNameEditText?.setOnChangeListener {
            bacsDelegate.inputData.holderName = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutHolderName.error = null
        }
        holderNameEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = bacsDelegate.outputData?.holderNameState?.validation
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
            bacsDelegate.inputData.bankAccountNumber = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutBankAccountNumber.error = null
        }
        bankAccountNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val bankAccountNumberValidation = bacsDelegate.outputData?.bankAccountNumberState?.validation
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
            bacsDelegate.inputData.sortCode = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSortCode.error = null
        }
        sortCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val sortCodeValidation = bacsDelegate.outputData?.sortCodeState?.validation
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
            bacsDelegate.inputData.shopperEmail = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutShopperEmail.error = null
        }
        shopperEmailEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val shopperEmailValidation = bacsDelegate.outputData?.shopperEmailState?.validation
            if (hasFocus) {
                binding.textInputLayoutShopperEmail.error = null
            } else if (shopperEmailValidation != null && shopperEmailValidation is Validation.Invalid) {
                binding.textInputLayoutShopperEmail.error = localizedContext.getString(shopperEmailValidation.reason)
            }
        }
    }

    private fun initConsentSwitches() {
        binding.switchConsentAmount.setOnCheckedChangeListener { _, isChecked ->
            bacsDelegate.inputData.isAmountConsentChecked = isChecked
            binding.textViewErrorConsentAmount.isVisible = !isChecked
            notifyInputDataChanged()
        }

        binding.switchConsentAccount.setOnCheckedChangeListener { _, isChecked ->
            bacsDelegate.inputData.isAccountConsentChecked = isChecked
            binding.textViewErrorConsentAccount.isVisible = !isChecked
            notifyInputDataChanged()
        }
    }

    private fun setAmountConsentSwitchText() {
        if (!bacsDelegate.configuration.amount.isEmpty) {
            val formattedAmount =
                CurrencyUtils.formatAmount(bacsDelegate.configuration.amount, bacsDelegate.configuration.shopperLocale)
            binding.switchConsentAmount.text =
                localizedContext.getString(R.string.bacs_consent_amount_specified, formattedAmount)
        } else {
            binding.switchConsentAmount.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_Bacs_Switch_Amount,
                localizedContext
            )
        }
    }

    private fun updateInputData(outputData: BacsDirectDebitOutputData) {
        bacsDelegate.inputData.apply {
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

    override fun getView(): View = this
}
