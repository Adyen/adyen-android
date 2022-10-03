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
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.bacs.databinding.BacsDirectDebitConfirmationViewBinding
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.ui.ComponentViewNew
import kotlinx.coroutines.CoroutineScope

class BacsDirectDebitConfirmationView @JvmOverloads constructor(
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

    private val binding: BacsDirectDebitConfirmationViewBinding =
        BacsDirectDebitConfirmationViewBinding.inflate(LayoutInflater.from(context), this)

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

        bacsDelegate.outputData?.let {
            binding.editTextHolderName.setText(it.holderNameState.value)
            binding.editTextBankAccountNumber.setText(it.bankAccountNumberState.value)
            binding.editTextSortCode.setText(it.sortCodeState.value)
            binding.editTextShopperEmail.setText(it.shopperEmailState.value)
        }
    }

    override val isConfirmationRequired = true

    override fun highlightValidationErrors() {
        // no ops
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
    }

    override fun getView(): View = this
}
