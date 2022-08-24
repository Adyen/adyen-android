/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2021.
 */

package com.adyen.checkout.voucher

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.api.LogoApi
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.voucher.databinding.VoucherViewBinding

class VoucherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<VoucherOutputData, VoucherConfiguration, ActionComponentData, VoucherComponent>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<VoucherOutputData> {

    private val binding: VoucherViewBinding = VoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var imageLoader: ImageLoader

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onComponentAttached() {
        imageLoader = ImageLoader.getInstance(context, component.configuration.environment)
    }

    override fun initView() {
        binding.textViewDownload.setOnClickListener {
            launchDownloadIntent()
        }
    }

    override val isConfirmationRequired: Boolean = false

    override fun highlightValidationErrors() {
        // no validation required
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewDescription.setLocalizedTextFromStyle(R.style.AdyenCheckout_Voucher_DescriptionTextAppearance)
        binding.textViewDownload.setLocalizedTextFromStyle(R.style.AdyenCheckout_Voucher_DownloadTextAppearance)
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(outputData: VoucherOutputData?) {
        if (outputData == null) return
        loadLogo(outputData.paymentMethodType)
    }

    private fun launchDownloadIntent() {
        val url = component.outputData?.downloadUrl ?: return
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()
        intent.launchUrl(context, Uri.parse(url))
    }

    private fun loadLogo(paymentMethodType: String?) {
        if (!paymentMethodType.isNullOrEmpty()) {
            imageLoader.load(paymentMethodType, binding.imageViewLogo, LogoApi.Size.MEDIUM)
        }
    }
}
