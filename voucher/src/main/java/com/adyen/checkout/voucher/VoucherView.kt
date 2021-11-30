/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2021.
 */

package com.adyen.checkout.voucher

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.voucher.databinding.VoucherViewBinding

class VoucherView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenLinearLayout<VoucherOutputData, VoucherConfiguration, ActionComponentData, VoucherComponent>(context, attrs, defStyleAttr),
    Observer<VoucherOutputData> {

    private val binding: VoucherViewBinding = VoucherViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onComponentAttached() {
        // no ops
    }

    override fun initView() {
        // TODO implement
    }

    override fun isConfirmationRequired(): Boolean {
        return false
    }

    override fun highlightValidationErrors() {
        // no validation required
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // TODO implement
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(outputData: VoucherOutputData?) {
        if (outputData == null) return
    }

}