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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader.Companion.getInstance
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.adapter.ClickableListRecyclerAdapter.OnItemCLickedListener
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

abstract class IssuerListRecyclerView<
    IssuerListPaymentMethodT : IssuerListPaymentMethod,
    IssuerListComponentT : IssuerListComponent<IssuerListPaymentMethodT>
    >
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenLinearLayout<IssuerListOutputData, IssuerListConfiguration, PaymentComponentState<IssuerListPaymentMethodT>, IssuerListComponentT>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<List<IssuerModel>>,
    OnItemCLickedListener {

    private lateinit var issuersRecyclerView: RecyclerView
    private lateinit var issuersAdapter: IssuerListRecyclerAdapter
    private val idealInputData = IssuerListInputData()

    // Regular View constructor
    init {
        LayoutInflater.from(getContext()).inflate(R.layout.issuer_list_recycler_view, this, true)
    }

    override fun initView() {
        issuersRecyclerView = findViewById<RecyclerView?>(R.id.recycler_issuers).apply {
            layoutManager = LinearLayoutManager(context)
        }
        issuersAdapter.setItemCLickListener(this)
        issuersRecyclerView.adapter = issuersAdapter
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun onComponentAttached() {
        issuersAdapter = IssuerListRecyclerAdapter(
            emptyList(),
            getInstance(context, component.configuration.environment),
            component.paymentMethodType,
            hideIssuersLogo()
        )
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.issuersLiveData.observe(lifecycleOwner, this)
    }

    override val isConfirmationRequired: Boolean
        get() = false

    override fun highlightValidationErrors() {
        // no implementation
    }

    open fun hideIssuersLogo(): Boolean {
        return false
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        issuersRecyclerView.isEnabled = enabled
    }

    override fun onChanged(issuerModels: List<IssuerModel>) {
        Logger.v(TAG, "onChanged")
        issuersAdapter.updateIssuerModelList(issuerModels)
    }

    override fun onItemClicked(position: Int) {
        Logger.d(TAG, "onItemClicked - $position")
        idealInputData.selectedIssuer = issuersAdapter.getIssuerAt(position)
        component.inputDataChanged(idealInputData)
    }

    companion object {
        private val TAG = getTag()
    }
}
