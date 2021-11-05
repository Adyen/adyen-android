/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.ui.view.AdyenLinearLayout

class BacsDirectDebitView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    AdyenLinearLayout<BacsDirectDebitOutputData, BacsDirectDebitConfiguration, GenericComponentState<BacsDirectDebitPaymentMethod>, BacsDirectDebitComponent>(context, attrs, defStyleAttr),
    Observer<BacsDirectDebitOutputData?> {

    override fun onComponentAttached() {
        TODO("Not yet implemented")
    }

    override fun initView() {
        TODO("Not yet implemented")
    }

    override fun isConfirmationRequired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun highlightValidationErrors() {
        TODO("Not yet implemented")
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        TODO("Not yet implemented")
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        TODO("Not yet implemented")
    }

    override fun onChanged(t: BacsDirectDebitOutputData?) {
        TODO("Not yet implemented")
    }
}