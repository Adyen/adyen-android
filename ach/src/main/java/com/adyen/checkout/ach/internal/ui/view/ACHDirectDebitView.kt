/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.ach.R
import com.adyen.checkout.ach.databinding.AchDirectDebitViewBinding
import com.adyen.checkout.ach.internal.ui.ACHDirectDebitDelegate
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitOutputData
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.old.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.old.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class ACHDirectDebitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr
),
    ComponentView {
    private val binding = AchDirectDebitViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: ACHDirectDebitDelegate

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is ACHDirectDebitDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)
        initAddressFormInput(coroutineScope)
        observeDelegate(delegate, coroutineScope)
        initBankAccountNumber()
        initAbaRoutingNumber()
        initAccountHolderName()

        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            delegate.updateInputData { isStorePaymentMethodSwitchChecked = isChecked }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        with(binding) {
            textviewAchHeader.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_ACHDirectDebit_AchHeaderTextView,
                localizedContext
            )
            textInputLayoutAccountHolderName.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_ACHDirectDebit_AccountHolderNameInput,
                localizedContext
            )
            textInputLayoutAccountNumber.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_ACHDirectDebit_AccountNumberInput,
                localizedContext
            )
            textInputLayoutAbaRoutingNumber.setLocalizedHintFromStyle(
                R.style.AdyenCheckout_ACHDirectDebit_AbaRoutingNumberInput,
                localizedContext
            )
            switchStorePaymentMethod.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_ACHDirectDebit_StorePaymentSwitch,
                localizedContext
            )
            addressFormInput.initLocalizedContext(localizedContext)
        }
    }

    private fun initBankAccountNumber() {
        with(binding) {
            editTextAccountNumber.setOnChangeListener {
                delegate.updateInputData { bankAccountNumber = editTextAccountNumber.rawValue }
                textInputLayoutAccountNumber.hideError()
            }

            editTextAccountNumber.setOnFocusChangeListener { _, hasFocus ->
                val accountNumberValidation = delegate.outputData.bankAccountNumber.validation
                if (hasFocus) {
                    textInputLayoutAccountNumber.hideError()
                } else if (accountNumberValidation is Validation.Invalid) {
                    textInputLayoutAccountNumber.showError(
                        localizedContext.getString(accountNumberValidation.reason)
                    )
                }
            }
        }
    }

    private fun initAbaRoutingNumber() {
        with(binding) {
            editTextAbaRoutingNumber.setOnChangeListener {
                delegate.updateInputData { bankLocationId = editTextAbaRoutingNumber.rawValue }
                textInputLayoutAbaRoutingNumber.hideError()
            }

            editTextAbaRoutingNumber.setOnFocusChangeListener { _, hasFocus ->
                val bankLocationIdValidation = delegate.outputData.bankLocationId.validation
                if (hasFocus) {
                    textInputLayoutAbaRoutingNumber.hideError()
                } else if (bankLocationIdValidation is Validation.Invalid) {
                    textInputLayoutAbaRoutingNumber.showError(
                        localizedContext.getString(bankLocationIdValidation.reason)
                    )
                }
            }
        }
    }

    private fun initAccountHolderName() {
        with(binding) {
            editTextAccountHolderName.setOnChangeListener {
                delegate.updateInputData { ownerName = editTextAccountHolderName.rawValue }
                textInputLayoutAccountHolderName.hideError()
            }

            editTextAccountHolderName.setOnFocusChangeListener { _, hasFocus ->
                val ownerNameValidation = delegate.outputData.ownerName.validation
                if (hasFocus) {
                    textInputLayoutAccountHolderName.hideError()
                } else if (ownerNameValidation is Validation.Invalid) {
                    textInputLayoutAccountHolderName.showError(
                        localizedContext.getString(ownerNameValidation.reason)
                    )
                }
            }
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(delegate, coroutineScope)
    }

    private fun observeDelegate(delegate: ACHDirectDebitDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(achOutputData: ACHDirectDebitOutputData) {
        setAddressInputVisibility(achOutputData.addressUIState)
        setStorePaymentSwitchVisibility(achOutputData.showStorePaymentField)
    }

    private fun setAddressInputVisibility(addressFormUIState: AddressFormUIState) {
        when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> {
                binding.addressFormInput.isVisible = true
            }
            else -> {
                binding.addressFormInput.isVisible = false
            }
        }
    }

    private fun setStorePaymentSwitchVisibility(showStorePaymentField: Boolean) {
        binding.switchStorePaymentMethod.isVisible = showStorePaymentField
    }

    override fun highlightValidationErrors() {
        with(delegate.outputData) {
            var isErrorFocused = false
            val ownerNameValidation = ownerName.validation
            if (ownerNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextAccountHolderName.requestFocus()
                binding.textInputLayoutAccountHolderName.showError(
                    localizedContext.getString(ownerNameValidation.reason)
                )
            }
            val accountNumberValidation = bankAccountNumber.validation
            if (accountNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutAccountNumber.requestFocus()
                }
                binding.textInputLayoutAccountNumber.showError(
                    localizedContext.getString(accountNumberValidation.reason)
                )
            }

            val abaRoutingNumberValidation = bankLocationId.validation
            if (abaRoutingNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutAbaRoutingNumber.requestFocus()
                }
                binding.textInputLayoutAbaRoutingNumber.showError(
                    localizedContext.getString(abaRoutingNumberValidation.reason)
                )
            }

            if (binding.addressFormInput.isVisible && !addressState.isValid) {
                binding.addressFormInput.highlightValidationErrors(isErrorFocused)
            }
        }
    }

    override fun getView(): View = this
}
