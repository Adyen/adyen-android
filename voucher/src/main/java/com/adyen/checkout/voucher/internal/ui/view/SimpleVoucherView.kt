/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.LogoSize
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.voucher.R
import com.adyen.checkout.voucher.databinding.SimpleVoucherViewBinding
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

internal open class SimpleVoucherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr,
    ),
    ComponentView {

    protected val binding = SimpleVoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: VoucherDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        this.setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is VoucherDelegate) { "Unsupported delegate type" }

        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        binding.textViewDownload.setOnClickListener { delegate.downloadVoucher(context) }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewDownload.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_DownloadTextAppearance,
            localizedContext,
        )
    }

    private fun observeDelegate(delegate: VoucherDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        adyenLog(AdyenLogLevel.DEBUG) { "outputDataChanged" }
        loadLogo(outputData.paymentMethodType)
        updateIntroductionText(outputData.introductionTextResource)
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

    private fun updateIntroductionText(@StringRes introductionTextResource: Int?) {
        if (introductionTextResource == null) return
        binding.textViewDescription.text = localizedContext.getString(introductionTextResource)
    }

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun getView(): View = this
}
