/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2021.
 */

package com.adyen.checkout.bacs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.bacs.databinding.BacsDirectDebitConfirmationViewBinding
import com.adyen.checkout.components.ui.view.AdyenLinearLayout

class BacsDirectDebitConfirmationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenLinearLayout<BacsDirectDebitOutputData,
        BacsDirectDebitConfiguration,
        BacsDirectDebitComponentState,
        BacsDirectDebitComponent>(context, attrs, defStyleAttr),
    Observer<BacsDirectDebitOutputData> {

    private val binding: BacsDirectDebitConfirmationViewBinding =
        BacsDirectDebitConfirmationViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onComponentAttached() {
        component.outputData?.let {
            binding.editTextHolderName.setText(it.holderNameState.value)
            binding.editTextBankAccountNumber.setText(it.bankAccountNumberState.value)
            binding.editTextSortCode.setText(it.sortCodeState.value)
            binding.editTextShopperEmail.setText(it.shopperEmailState.value)
        }
        component.setConfirmationMode()
    }

    override fun initView() {
        // no ops
    }

    override val isConfirmationRequired = true

    override fun highlightValidationErrors() {
        // no ops
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        val attrs = intArrayOf(android.R.attr.hint)

        // Holder Name
        var typedArray = localizedContext.obtainStyledAttributes(R.style.Adyen_Checkout_Bacs_HolderNameInput, attrs)
        binding.textInputLayoutHolderName.hint = typedArray.getString(0)
        typedArray.recycle()

        // Account Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.Adyen_Checkout_Bacs_AccountNumberInput, attrs)
        binding.textInputLayoutBankAccountNumber.hint = typedArray.getString(0)
        typedArray.recycle()

        // Sort Code
        typedArray = localizedContext.obtainStyledAttributes(R.style.Adyen_Checkout_Bacs_SortCodeInput, attrs)
        binding.textInputLayoutSortCode.hint = typedArray.getString(0)
        typedArray.recycle()

        // Shopper Email
        typedArray = localizedContext.obtainStyledAttributes(R.style.Adyen_Checkout_Bacs_ShopperEmailInput, attrs)
        binding.textInputLayoutShopperEmail.hint = typedArray.getString(0)
        typedArray.recycle()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(t: BacsDirectDebitOutputData?) {
        // no ops
    }
}
