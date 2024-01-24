/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 15/3/2023.
 */

package com.adyen.checkout.voucher.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.copyTextToClipboard
import com.adyen.checkout.components.core.internal.util.isEmpty
import com.adyen.checkout.components.core.internal.util.toast
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.LogoSize
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.voucher.R
import com.adyen.checkout.voucher.databinding.FullVoucherViewBinding
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate
import com.adyen.checkout.voucher.internal.ui.model.VoucherInformationField
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import com.adyen.checkout.voucher.internal.ui.model.VoucherStoreAction
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent.Failure
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent.PermissionDenied
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class FullVoucherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context,
        attrs,
        defStyleAttr,
    ),
    ComponentView {

    private val binding: FullVoucherViewBinding = FullVoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context
    private lateinit var delegate: VoucherDelegate

    private var informationFieldsAdapter: VoucherInformationFieldsAdapter? = null
    private var coroutineScope: CoroutineScope? = null

    init {
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        this.setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is VoucherDelegate) { "Unsupported delegate type" }

        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)
        this.coroutineScope = coroutineScope

        binding.buttonCopyCode.setOnClickListener { copyCode(delegate.outputData.reference) }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewPaymentReference.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_PaymentReference,
            localizedContext,
        )
    }

    private fun observeDelegate(delegate: VoucherDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)

        delegate.eventFlow
            .onEach { handleEventFlow(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        Logger.d(TAG, "outputDataChanged")

        loadLogo(outputData.paymentMethodType)
        updateIntroductionText(outputData.introductionTextResource)
        updateAmount(outputData.totalAmount)
        updateCodeReference(outputData.reference)
        updateStoreActionButton(outputData.storeAction)
        updateInformationFields(outputData.informationFields)
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
        binding.textViewIntroduction.text = localizedContext.getString(introductionTextResource)
    }

    private fun updateAmount(amount: Amount?) = with(binding) {
        if (amount != null && !amount.isEmpty) {
            val formattedAmount = CurrencyUtils.formatAmount(
                amount,
                delegate.componentParams.shopperLocale,
            )
            textViewAmount.isVisible = true
            textViewAmount.text = formattedAmount
        } else {
            textViewAmount.isVisible = false
        }
    }

    private fun updateCodeReference(codeReference: String?) = with(binding) {
        textViewReferenceCode.text = codeReference

        val isVisible = !codeReference.isNullOrEmpty()
        textViewReferenceCode.isVisible = isVisible
        buttonCopyCode.isVisible = isVisible
    }

    private fun updateStoreActionButton(storeAction: VoucherStoreAction?) = with(binding) {
        buttonStore.isVisible = storeAction != null

        if (storeAction == null) return
        when (storeAction) {
            is VoucherStoreAction.DownloadPdf -> {
                buttonStore.text = localizedContext.getString(R.string.checkout_voucher_download_pdf)
                buttonStore.setOnClickListener { onDownloadPdfClicked() }
            }

            VoucherStoreAction.SaveAsImage -> {
                buttonStore.text = localizedContext.getString(R.string.checkout_voucher_save_image)
                buttonStore.setOnClickListener { onSaveAsImageClicked() }
            }
        }
    }

    private fun updateInformationFields(informationFields: List<VoucherInformationField>?) {
        if (informationFields.isNullOrEmpty()) return
        if (informationFieldsAdapter == null) {
            informationFieldsAdapter = VoucherInformationFieldsAdapter()
            binding.recyclerViewInformationFields.adapter = informationFieldsAdapter
        }
        informationFieldsAdapter?.submitList(informationFields)
    }

    private fun onDownloadPdfClicked() {
        delegate.downloadVoucher(context)
    }

    private fun onSaveAsImageClicked() {
        binding.layoutButtons.isVisible = false
        doOnNextLayout {
            delegate.saveVoucherAsImage(context, this)
            binding.layoutButtons.isVisible = true
        }
    }

    private fun copyCode(codeReference: String?) {
        codeReference ?: return
        context.copyTextToClipboard(
            COPY_LABEL,
            codeReference,
            localizedContext.getString(R.string.checkout_voucher_copied_toast),
        )
    }

    private fun handleEventFlow(event: VoucherUIEvent) {
        when (event) {
            Success -> {
                context.toast(localizedContext.getString(R.string.checkout_voucher_image_saved))
            }

            PermissionDenied -> {
                context.toast(localizedContext.getString(R.string.checkout_voucher_permission_denied))
            }

            is Failure -> {
                context.toast(localizedContext.getString(R.string.checkout_voucher_image_failed))
            }
        }
    }

    override fun highlightValidationErrors() {
        // No validation required
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
        private const val COPY_LABEL = "Voucher code reference"
    }
}
