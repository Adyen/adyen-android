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
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.copyTextToClipboard
import com.adyen.checkout.components.core.internal.util.isEmpty
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal open class FullVoucherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    protected val binding: FullVoucherViewBinding = FullVoucherViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context
    private lateinit var delegate: VoucherDelegate

    private var informationFieldsAdapter: VoucherInformationFieldsAdapter? = null

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

        binding.buttonCopyCode.setOnClickListener { copyCode(delegate.outputData.reference) }
        binding.buttonDownloadPdf.setOnClickListener { delegate.downloadVoucher(context) }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewPaymentReference.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_PaymentReference,
            localizedContext
        )
        binding.buttonCopyCode.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_ButtonCopyCode,
            localizedContext
        )
        binding.buttonDownloadPdf.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_ButtonDownloadPdf,
            localizedContext
        )
        // TODO: Perhaps keep this style and move it to Boleto implementation
//        binding.textViewExpirationLabel.setLocalizedTextFromStyle(
//            R.style.AdyenCheckout_Voucher_ExpirationDateLabel,
//            localizedContext
//        )
    }

    private fun observeDelegate(delegate: VoucherDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: VoucherOutputData) {
        Logger.d(TAG, "outputDataChanged")

        loadLogo(outputData.paymentMethodType)
        updateIntroductionText(outputData.introductionTextResource)
        updateAmount(outputData.totalAmount)
        updateCodeReference(outputData.reference)
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

    private fun updateAmount(amount: Amount?) {
        if (amount != null && !amount.isEmpty) {
            val formattedAmount = CurrencyUtils.formatAmount(
                amount,
                delegate.componentParams.shopperLocale
            )
            binding.textViewAmount.isVisible = true
            binding.textViewAmount.text = formattedAmount
        } else {
            binding.textViewAmount.isVisible = false
        }
    }

    private fun updateCodeReference(codeReference: String?) {
        binding.textViewReferenceCode.text = codeReference

        val isVisible = !codeReference.isNullOrEmpty()
        binding.textViewReferenceCode.isVisible = isVisible
        binding.buttonCopyCode.isVisible = isVisible
    }

    private fun updateInformationFields(informationFields: List<VoucherInformationField>?) {
        if (informationFields.isNullOrEmpty()) return
        if (informationFieldsAdapter == null) {
            informationFieldsAdapter = VoucherInformationFieldsAdapter()
            binding.listViewInformationFields.adapter = informationFieldsAdapter
        }
        informationFieldsAdapter?.submitList(informationFields)
    }

    private fun copyCode(codeReference: String?) {
        codeReference ?: return
        context.copyTextToClipboard(
            COPY_LABEL,
            codeReference,
            localizedContext.getString(R.string.checkout_voucher_copied_toast)
        )
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
