/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */
package com.adyen.checkout.issuerlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader.Companion.getInstance
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.d

@Suppress("TooManyFunctions")
abstract class IssuerListSpinnerView<
    IssuerListPaymentMethodT : IssuerListPaymentMethod,
    IssuerListComponentT : IssuerListComponent<IssuerListPaymentMethodT>
    >
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<
        IssuerListOutputData,
        IssuerListConfiguration,
        PaymentComponentState<IssuerListPaymentMethodT>,
        IssuerListComponentT>(context, attrs, defStyleAttr),
    AdapterView.OnItemSelectedListener {

    private lateinit var issuersSpinner: AppCompatSpinner
    private lateinit var issuersAdapter: IssuerListSpinnerAdapter

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.issuer_list_spinner_view, this, true)
    }

    override fun initView() {
        issuersSpinner = findViewById<AppCompatSpinner?>(R.id.spinner_issuers).apply {
            adapter = issuersAdapter
            onItemSelectedListener = this@IssuerListSpinnerView
        }
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun onComponentAttached() {
        issuersAdapter = IssuerListSpinnerAdapter(
            context,
            component.issuers,
            getInstance(context, component.configuration.environment),
            component.paymentMethodType,
            hideIssuersLogo()
        )
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) = Unit

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        // no implementation
    }

    open fun hideIssuersLogo(): Boolean {
        return false
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
        component.inputData.selectedIssuer = issuersAdapter.getItem(position)
        component.notifyInputDataChanged()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        issuersSpinner.isEnabled = enabled
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // nothing changed
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
