/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcz.databinding.OnlineBankingCzSpinnerLayoutBinding

class OnlineBankingCZView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<
        OnlineBankingOutputData,
        OnlineBankingConfiguration,
        PaymentComponentState<OnlineBankingCZPaymentMethod>,
        OnlineBankingCZComponent
        >(context, attrs, defStyleAttr),
    AdapterView.OnItemSelectedListener {

    private val binding: OnlineBankingCzSpinnerLayoutBinding =
        OnlineBankingCzSpinnerLayoutBinding.inflate(LayoutInflater.from(context), this)

    private val issuersAdapter: SimpleTextListAdapter<OnlineBankingModel> = SimpleTextListAdapter(context)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun onComponentAttached() {
        issuersAdapter.setItems(component.issuers)
    }

    override fun initView() {
        binding.autoCompleteTextViewIssuers.apply {
            inputType = 0
            setAdapter(issuersAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                Logger.d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
                component.inputData.selectedIssuer = issuersAdapter.getItem(position)
                component.notifyInputDataChanged()
            }
        }
        binding.textviewTermsAndConditions.setOnClickListener { component.openTermsAndConditionsPdf(context) }
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        // no implementation
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) = Unit

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        Logger.d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
        component.inputData.selectedIssuer = issuersAdapter.getItem(position)
        component.notifyInputDataChanged()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // nothing changed
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.autoCompleteTextViewIssuers.isEnabled = enabled
        binding.textInputLayoutIssuers.isEnabled = enabled
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
