/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/12/2023.
 */

package com.adyen.checkout.voucher.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.LogoSize
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.voucher.R
import com.adyen.checkout.voucher.databinding.BacsVoucherViewBinding
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class BacsVoucherView @JvmOverloads constructor(
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

    private val binding: BacsVoucherViewBinding = BacsVoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: VoucherDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is VoucherDelegate) { "Unsupported delegate type" }

        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        binding.textViewDownload.setOnClickListener { delegate.storeVoucher(context) }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_Description_Bacs,
            localizedContext
        )
        binding.textViewDownload.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_DownloadTextAppearance,
            localizedContext
        )
    }

    private fun observeDelegate(delegate: VoucherDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        Logger.d(TAG, "outputDataChanged")
        loadLogo(outputData.paymentMethodType)
    }

    private fun loadLogo(paymentMethodType: String?) {
        if (!paymentMethodType.isNullOrEmpty()) {
            binding.imageViewLogo.loadLogo(
                environment = delegate.componentParams.environment,
                txVariant = paymentMethodType,
                size = LogoSize.MEDIUM,
            )
        }
    }

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
