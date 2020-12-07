/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.stored

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel

class PreselectedStoredPaymentViewModel(storedPaymentMethod: StoredPaymentMethod) : ViewModel() {

    private val storedPaymentMethodMutableLiveData: MutableLiveData<StoredPaymentMethodModel> = MutableLiveData()
    val storedPaymentLiveData: LiveData<StoredPaymentMethodModel> = storedPaymentMethodMutableLiveData

    init {
        storedPaymentMethodMutableLiveData.value = makeStoredModel(storedPaymentMethod)
    }
}
