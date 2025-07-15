/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.paybybankus.R
import com.adyen.checkout.paybybankus.databinding.PayByBankUsViewBinding
import com.adyen.checkout.paybybankus.internal.PayByBankUSDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem
import com.adyen.checkout.ui.core.old.internal.ui.view.LogoTextAdapter
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UiCoreR

internal class PayByBankUSView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: PayByBankUsViewBinding = PayByBankUsViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: PayByBankUSDelegate

    private var logoTextAdapter: LogoTextAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UiCoreR.dimen.standard_margin).toInt()
        setPadding(0, padding, 0, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is PayByBankUSDelegate) { "Unsupported delegate type" }
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initPayByBankUSLogo()
        initBrandList(delegate.outputData.brandList)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewTitle.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayByBankUS_Title,
            localizedContext,
        )

        binding.textViewDisclaimerHeader.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayByBankUS_DisclaimerHeader,
            localizedContext,
        )

        binding.textViewDisclaimerBody.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayByBankUS_DisclaimerBody,
            localizedContext,
        )
    }

    private fun initPayByBankUSLogo() {
        binding.imageViewLogo.loadLogo(
            delegate.componentParams.environment,
            delegate.getPaymentMethodType(),
        )
    }

    private fun initBrandList(brands: List<LogoTextItem>) {
        if (logoTextAdapter == null) {
            logoTextAdapter = LogoTextAdapter(localizedContext)
            binding.recyclerViewBrandList.adapter = logoTextAdapter
        }
        logoTextAdapter?.submitList(brands)
    }

    override fun highlightValidationErrors() {
        // no validation
    }

    override fun getView(): View = this
}
