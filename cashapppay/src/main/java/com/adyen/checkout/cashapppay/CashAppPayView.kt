/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/4/2023.
 */

package com.adyen.checkout.cashapppay

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.cashapppay.databinding.CashAppPayViewBinding
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

/**
 * View for the [CashAppPayComponent]. Use your own submit button with this view.
 *
 * This view is not needed if you disabled the store payment switch, the component will automatically submit in this case. You can also use the
 * [isConfirmationRequired] function to check whether you need to show this view or not.
 */
class CashAppPayView :
    AdyenLinearLayout<CashAppPayOutputData, CashAppPayConfiguration, GenericComponentState<CashAppPayPaymentMethod>, CashAppPayComponent>,
    Observer<CashAppPayOutputData> {

    private val binding: CashAppPayViewBinding = CashAppPayViewBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init()
    }

    private fun init() {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.switchStorePaymentMethod.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_CashAppPay_StorePaymentSwitch,
            localizedContext
        )
    }

    override fun initView() {
        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            component.inputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
        binding.switchStorePaymentMethod.isVisible = component.showStorePaymentField()
        notifyInputDataChanged()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onComponentAttached() {
        // nothing to impl
    }

    override fun onChanged(cashAppPayOutputData: CashAppPayOutputData?) {
        Logger.v(TAG, "CashAppPayOutputData changed")
    }


    override fun isConfirmationRequired(): Boolean {
        return component.isUserInteractionRequired()
    }

    override fun highlightValidationErrors() {
        // nothing to impl
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(component.inputData)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
