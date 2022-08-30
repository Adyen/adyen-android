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
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.d
import com.google.android.material.textfield.TextInputLayout

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

    protected val termsAndConditionsTextView: TextView?
        get() = rootView.findViewById<TextView>(R.id.textview_termsAndConditions)
    private val autoCompleteTextViewIssuers: AutoCompleteTextView
        get() = rootView.findViewById(R.id.autoCompleteTextView_issuers)
    private val textInputLayoutIssuers: TextInputLayout
        get() = rootView.findViewById(R.id.textInputLayout_issuers)

    private val issuersAdapter: SimpleTextListAdapter<IssuerModel> = SimpleTextListAdapter(context)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(getContext()).inflate(R.layout.issuer_list_spinner_view, this, true)
    }

    override fun initView() {
        autoCompleteTextViewIssuers.apply {
            inputType = 0
            setAdapter(issuersAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
                component.inputData.selectedIssuer = issuersAdapter.getItem(position)
                component.notifyInputDataChanged()
            }
        }
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun onComponentAttached() {
        issuersAdapter.setItems(component.issuers)
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
        autoCompleteTextViewIssuers.isEnabled = enabled
        textInputLayoutIssuers.isEnabled = enabled
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // nothing changed
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
