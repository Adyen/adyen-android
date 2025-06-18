/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.paybybank.R
import com.adyen.checkout.paybybank.databinding.PayByBankViewBinding
import com.adyen.checkout.paybybank.internal.ui.PayByBankDelegate
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

internal class PayByBankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: PayByBankViewBinding = PayByBankViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: PayByBankDelegate

    private var payByBankRecyclerAdapter: PayByBankRecyclerAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(0, padding, 0, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is PayByBankDelegate) { "Unsupported delegate type" }
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        initSearchQueryInput()
        initIssuersRecyclerView()
    }

    private fun observeDelegate(delegate: PayByBankDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { onOutputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun onOutputDataChanged(outputData: PayByBankOutputData) {
        payByBankRecyclerAdapter?.submitList(outputData.issuers)
        binding.textViewNoMatchingIssuers.isVisible = outputData.issuers.isEmpty()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutSearchQuery.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_PayByBank_SearchQueryInput,
            localizedContext,
        )
        binding.textViewNoMatchingIssuers.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayByBank_NoMatchingIssuers,
            localizedContext,
        )
    }

    private fun onItemClicked(issuerModel: IssuerModel) {
        adyenLog(AdyenLogLevel.DEBUG) { "onItemClicked - ${issuerModel.name}" }
        delegate.updateInputData { selectedIssuer = issuerModel }
        delegate.onSubmit()
    }

    private fun initSearchQueryInput() {
        binding.editTextSearchQuery.setOnChangeListener {
            delegate.updateInputData { query = it.toString() }
        }
    }

    private fun initIssuersRecyclerView() {
        payByBankRecyclerAdapter = PayByBankRecyclerAdapter(
            paymentMethod = delegate.getPaymentMethodType(),
            onItemClicked = ::onItemClicked,
        ).apply {
            submitList(delegate.getIssuers())
        }
        binding.recyclerIssuers.adapter = payByBankRecyclerAdapter
    }

    override fun highlightValidationErrors() {
        // no validation
    }

    override fun getView(): View = this
}
