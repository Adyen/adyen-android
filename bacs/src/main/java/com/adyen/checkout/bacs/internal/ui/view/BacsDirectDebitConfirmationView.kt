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
import android.widget.LinearLayout
import com.adyen.checkout.bacs.R
import com.adyen.checkout.bacs.databinding.BacsDirectDebitConfirmationViewBinding
import com.adyen.checkout.bacs.internal.ui.BacsDirectDebitDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class BacsDirectDebitConfirmationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: BacsDirectDebitConfirmationViewBinding =
        BacsDirectDebitConfirmationViewBinding.inflate(LayoutInflater.from(context), this)

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

        with(bacsDelegate.outputData) {
            binding.editTextHolderName.setText(holderNameState.value)
            binding.editTextBankAccountNumber.setText(bankAccountNumberState.value)
            binding.editTextSortCode.setText(sortCodeState.value)
            binding.editTextShopperEmail.setText(shopperEmailState.value)
        }
    }

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
