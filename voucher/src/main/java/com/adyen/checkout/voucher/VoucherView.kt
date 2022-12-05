/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.imageloader.DefaultImageLoader
import com.adyen.checkout.components.imageloader.LogoSize
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.voucher.databinding.VoucherViewBinding
import kotlinx.coroutines.CoroutineScope
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

    private lateinit var imageLoader: DefaultImageLoader

    private lateinit var localizedContext: Context

    override val isConfirmationRequired: Boolean = false

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is VoucherDelegate) throw IllegalArgumentException("Unsupported delegate type")

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        imageLoader = DefaultImageLoader.with(delegate.componentParams.environment)
        imageLoader.initialize(coroutineScope)

        observeDelegate(delegate, coroutineScope)

        binding.textViewDownload.setOnClickListener { launchDownloadIntent(delegate.outputData.downloadUrl) }
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
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        Logger.d(TAG, "outputDataChanged")
        loadLogo(outputData.paymentMethodType)
    }

    private fun loadLogo(paymentMethodType: String?) {
        if (!paymentMethodType.isNullOrEmpty()) {
            imageLoader.loadLogo(
                paymentMethodType,
                binding.imageViewLogo,
                LogoSize.MEDIUM,
                R.drawable.ic_placeholder_image,
                R.drawable.ic_placeholder_image
            )
        }
    }

    private fun launchDownloadIntent(url: String?) {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()
            .launchUrl(context, Uri.parse(url))
    }

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
