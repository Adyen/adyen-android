/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */
package com.adyen.checkout.issuerlist.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.issuerlist.databinding.IssuerListSpinnerViewBinding
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope

internal class IssuerListSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr,
    ),
    ComponentView,
    AdapterView.OnItemSelectedListener {

    private val binding: IssuerListSpinnerViewBinding =
        IssuerListSpinnerViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var issuersAdapter: IssuerListSpinnerAdapter

    private lateinit var localizedContext: Context

    private lateinit var issuerListDelegate: IssuerListDelegate<*, *>

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is IssuerListDelegate<*, *>) { "Unsupported delegate type" }
        issuerListDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        issuersAdapter = IssuerListSpinnerAdapter(
            context = context,
            issuerList = delegate.getIssuers(),
            paymentMethod = delegate.getPaymentMethodType(),
            hideIssuerLogo = delegate.componentParams.hideIssuerLogos,
        )
        binding.spinnerIssuers.apply {
            adapter = issuersAdapter
            onItemSelectedListener = this@IssuerListSpinnerView
        }
    }

    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
    private fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun highlightValidationErrors() {
        // no implementation
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = issuersAdapter.getItem(position)
        adyenLog(AdyenLogLevel.DEBUG) { "onItemSelected - " + item.name }
        issuerListDelegate.updateInputData { selectedIssuer = item }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.spinnerIssuers.isEnabled = enabled
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // nothing changed
    }

    override fun getView(): View = this
}
