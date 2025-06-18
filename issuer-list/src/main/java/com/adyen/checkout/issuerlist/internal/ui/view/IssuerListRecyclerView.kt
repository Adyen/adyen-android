/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */
package com.adyen.checkout.issuerlist.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.issuerlist.databinding.IssuerListRecyclerViewBinding
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope

internal class IssuerListRecyclerView @JvmOverloads constructor(
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

    private val binding: IssuerListRecyclerViewBinding =
        IssuerListRecyclerViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var issuerListDelegate: IssuerListDelegate<*, *>

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is IssuerListDelegate<*, *>) { "Unsupported delegate type" }
        issuerListDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        binding.recyclerIssuers.adapter = IssuerListRecyclerAdapter(
            paymentMethod = delegate.getPaymentMethodType(),
            hideIssuerLogo = delegate.componentParams.hideIssuerLogos,
            onItemClicked = ::onItemClicked,
        ).apply {
            submitList(delegate.getIssuers())
        }
    }

    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
    private fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun highlightValidationErrors() {
        // no ops
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.recyclerIssuers.isEnabled = enabled
    }

    private fun onItemClicked(issuerModel: IssuerModel) {
        adyenLog(AdyenLogLevel.DEBUG) { "onItemClicked - ${issuerModel.name}" }
        issuerListDelegate.updateInputData { selectedIssuer = issuerModel }
        issuerListDelegate.onSubmit()
    }

    override fun getView(): View = this
}
