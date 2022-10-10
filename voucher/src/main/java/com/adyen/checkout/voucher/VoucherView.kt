/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.api.LogoApi
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.voucher.databinding.VoucherViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class VoucherView @JvmOverloads constructor(
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

    private val binding: VoucherViewBinding = VoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var imageLoader: ImageLoader

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is VoucherDelegate) throw IllegalArgumentException("Unsupported delegate type")

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        imageLoader = ImageLoader.getInstance(context, delegate.configuration.environment)

        observeDelegate(delegate, coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_DescriptionTextAppearance,
            localizedContext
        )
        binding.textViewDownload.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_DownloadTextAppearance,
            localizedContext
        )
    }

    private fun observeDelegate(delegate: VoucherDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .filterNotNull()
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        Logger.d(TAG, "outputDataChanged")
        loadLogo(outputData.paymentMethodType)
    }

    override val isConfirmationRequired: Boolean = false

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun getView(): View = this

    private fun loadLogo(paymentMethodType: String?) {
        if (!paymentMethodType.isNullOrEmpty()) {
            imageLoader.load(paymentMethodType, binding.imageViewLogo, LogoApi.Size.MEDIUM)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
