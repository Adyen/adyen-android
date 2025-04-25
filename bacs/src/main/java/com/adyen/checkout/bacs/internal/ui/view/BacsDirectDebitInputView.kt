/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.bacs.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.bacs.R
import com.adyen.checkout.bacs.databinding.BacsDirectDebitInputViewBinding
import com.adyen.checkout.bacs.internal.ui.BacsDirectDebitDelegate
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitOutputData
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class BacsDirectDebitInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = BacsDirectDebitInputViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var bacsDelegate: BacsDirectDebitDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is BacsDirectDebitDelegate) { "Unsupported delegate type" }
        bacsDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        bacsDelegate.outputData.let {
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

    override fun highlightValidationErrors() {
        bacsDelegate.outputData.let {
            var isErrorFocused = false
            val holderNameValidation = it.holderNameState.validation
            if (holderNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextHolderName.requestFocus()
                binding.textInputLayoutHolderName.showError(localizedContext.getString(holderNameValidation.reason))
            }
            val bankAccountNumberValidation = it.bankAccountNumberState.validation
            if (bankAccountNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextBankAccountNumber.requestFocus()
                }
                binding.textInputLayoutBankAccountNumber.showError(
                    localizedContext.getString(bankAccountNumberValidation.reason),
                )
            }
            val sortCodeValidation = it.sortCodeState.validation
            if (sortCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextSortCode.requestFocus()
                }
                binding.textInputLayoutSortCode.showError(localizedContext.getString(sortCodeValidation.reason))
            }
            val shopperEmailValidation = it.shopperEmailState.validation
            if (shopperEmailValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    binding.editTextShopperEmail.requestFocus()
                }
                binding.textInputLayoutShopperEmail.showError(localizedContext.getString(shopperEmailValidation.reason))
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
                    @Suppress("UNUSED_VALUE")
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
            localizedContext,
        )
        binding.textInputLayoutBankAccountNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_AccountNumberInput,
            localizedContext,
        )
        binding.textInputLayoutSortCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_SortCodeInput,
            localizedContext,
        )
        binding.textInputLayoutShopperEmail.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Bacs_ShopperEmailInput,
            localizedContext,
        )
        binding.switchConsentAccount.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Bacs_Switch_Account,
            localizedContext,
        )
        setAmountConsentSwitchText(bacsDelegate.componentParams)
    }

    private fun observeDelegate(delegate: BacsDirectDebitDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(bacsDirectDebitOutputData: BacsDirectDebitOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "bacsDirectDebitOutputData changed" }

        onBankAccountNumberValidated(bacsDirectDebitOutputData.bankAccountNumberState)
        onSortCodeValidated(bacsDirectDebitOutputData.sortCodeState)
    }

    private fun initHolderNameInput() {
        binding.editTextHolderName.apply {
            setOnChangeListener {
                bacsDelegate.updateInputData { holderName = it.toString() }
                binding.textInputLayoutHolderName.hideError()
            }

            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val holderNameValidation = bacsDelegate.outputData.holderNameState.validation
                if (hasFocus) {
                    binding.textInputLayoutHolderName.hideError()
                } else if (holderNameValidation is Validation.Invalid) {
                    binding.textInputLayoutHolderName.showError(localizedContext.getString(holderNameValidation.reason))
                }
            }

            requestFocus()
        }
    }

    private fun initBankAccountNumberInput() {
        val bankAccountNumberEditText = binding.editTextBankAccountNumber as? AdyenTextInputEditText
        bankAccountNumberEditText?.setOnChangeListener {
            bacsDelegate.updateInputData { bankAccountNumber = it.toString() }
            binding.textInputLayoutBankAccountNumber.hideError()
        }
        bankAccountNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val bankAccountNumberValidation = bacsDelegate.outputData.bankAccountNumberState.validation
            if (hasFocus) {
                binding.textInputLayoutBankAccountNumber.hideError()
            } else if (bankAccountNumberValidation is Validation.Invalid) {
                binding.textInputLayoutBankAccountNumber.showError(
                    localizedContext.getString(bankAccountNumberValidation.reason),
                )
            }
        }
    }

    private fun initSortCodeInput() {
        val sortCodeEditText = binding.editTextSortCode as? AdyenTextInputEditText
        sortCodeEditText?.setOnChangeListener {
            bacsDelegate.updateInputData { sortCode = it.toString() }
            binding.textInputLayoutSortCode.hideError()
        }
        sortCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val sortCodeValidation = bacsDelegate.outputData.sortCodeState.validation
            if (hasFocus) {
                binding.textInputLayoutSortCode.hideError()
            } else if (sortCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSortCode.showError(localizedContext.getString(sortCodeValidation.reason))
            }
        }
    }

    private fun initShopperEmailInput() {
        val shopperEmailEditText = binding.editTextShopperEmail as? AdyenTextInputEditText
        shopperEmailEditText?.setOnChangeListener {
            bacsDelegate.updateInputData { shopperEmail = it.toString().trim() }
            binding.textInputLayoutShopperEmail.hideError()
        }
        shopperEmailEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val shopperEmailValidation = bacsDelegate.outputData.shopperEmailState.validation
            if (hasFocus) {
                binding.textInputLayoutShopperEmail.hideError()
            } else if (shopperEmailValidation is Validation.Invalid) {
                binding.textInputLayoutShopperEmail.showError(localizedContext.getString(shopperEmailValidation.reason))
            }
        }
    }

    private fun initConsentSwitches() {
        binding.switchConsentAmount.setOnCheckedChangeListener { _, isChecked ->
            bacsDelegate.updateInputData { isAmountConsentChecked = isChecked }
            binding.textViewErrorConsentAmount.isVisible = !isChecked
        }

        binding.switchConsentAccount.setOnCheckedChangeListener { _, isChecked ->
            bacsDelegate.updateInputData { isAccountConsentChecked = isChecked }
            binding.textViewErrorConsentAccount.isVisible = !isChecked
        }
    }

    private fun setAmountConsentSwitchText(componentParams: ButtonComponentParams) {
        val amount = componentParams.amount
        if (amount != null) {
            val formattedAmount = CurrencyUtils.formatAmount(
                amount,
                componentParams.shopperLocale,
            )
            binding.switchConsentAmount.text =
                localizedContext.getString(R.string.bacs_consent_amount_specified, formattedAmount)
        } else {
            binding.switchConsentAmount.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_Bacs_Switch_Amount,
                localizedContext,
            )
        }
    }

    private fun updateInputData(outputData: BacsDirectDebitOutputData) {
        bacsDelegate.updateInputData {
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
