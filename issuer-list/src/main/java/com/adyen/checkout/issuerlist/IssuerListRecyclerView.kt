/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */
package com.adyen.checkout.issuerlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.api.ImageLoader.Companion.getInstance
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.databinding.IssuerListRecyclerViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class IssuerListRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentViewNew {

    private val binding: IssuerListRecyclerViewBinding =
        IssuerListRecyclerViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var issuersAdapter: IssuerListRecyclerAdapter

    private lateinit var localizedContext: Context

    private lateinit var issuerListDelegate: IssuerListDelegate<*>

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is IssuerListDelegate<*>) throw IllegalArgumentException("Unsupported delegate type")
        issuerListDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        issuersAdapter = IssuerListRecyclerAdapter(
            issuerModelList = delegate.getIssuers(),
            imageLoader = getInstance(context, delegate.configuration.environment),
            paymentMethod = delegate.getPaymentMethodType(),
            hideIssuerLogo = delegate.configuration.hideIssuerLogos,
        ).apply {
            setItemCLickListener(::onItemClicked)
        }
        binding.recyclerIssuers.adapter = issuersAdapter
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    private fun observeDelegate(delegate: IssuerListDelegate<*>, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(issuerListOutputData: IssuerListOutputData?) {
        // no ops
    }

    override val isConfirmationRequired: Boolean = false

    override fun highlightValidationErrors() {
        // no ops
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.recyclerIssuers.isEnabled = enabled
    }

    private fun onItemClicked(position: Int) {
        Logger.d(TAG, "onItemClicked - $position")
        issuerListDelegate.inputData.selectedIssuer = issuersAdapter.getIssuerAt(position)
        issuerListDelegate.onInputDataChanged(issuerListDelegate.inputData)
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
