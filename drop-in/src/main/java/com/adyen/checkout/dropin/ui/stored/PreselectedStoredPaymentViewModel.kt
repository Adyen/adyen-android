/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.stored

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.AwaitingComponentInitialization
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.Idle
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.RequestPayment
import com.adyen.checkout.dropin.ui.stored.PreselectedStoredState.ShowStoredPaymentDialog

class PreselectedStoredPaymentViewModel(
    storedPaymentMethod: StoredPaymentMethod,
    private val componentRequiresInput: Boolean
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private val storedPaymentMethodMutableLiveData: MutableLiveData<StoredPaymentMethodModel> = MutableLiveData()
    val storedPaymentLiveData: LiveData<StoredPaymentMethodModel> = storedPaymentMethodMutableLiveData

    private val componentFragmentStateMutable = MutableLiveData<PreselectedStoredState>(Idle)
    val componentFragmentState: LiveData<PreselectedStoredState> = componentFragmentStateMutable

    private var componentState: PaymentComponentState<PaymentMethodDetails>? = null

    init {
        storedPaymentMethodMutableLiveData.value = makeStoredModel(storedPaymentMethod)
    }

    fun componentStateChanged(componentState: PaymentComponentState<in PaymentMethodDetails>) {
        val fragmentState = componentFragmentStateMutable.value
        Log.d(TAG, "componentStateChanged - componentState.isReady: ${componentState.isReady} - " +
            "fragmentState: $fragmentState")
        this.componentState = componentState
        if (!componentRequiresInput && componentState.isReady && fragmentState is AwaitingComponentInitialization) {
            val state = RequestPayment(componentState)
            Log.d(TAG, "componentStateChanged - setting fragment state $state")
            componentFragmentStateMutable.value = state
        }
    }

    fun payButtonClicked() {
        val fragmentState = componentFragmentStateMutable.value
        val componentState = componentState
        Log.d(TAG, "payButtonClicked - componentState.isReady: ${componentState?.isReady} - " +
            "fragmentState: $fragmentState")
        val state = when {
            componentRequiresInput -> ShowStoredPaymentDialog
            componentState?.isReady == true -> RequestPayment(componentState)
            else -> AwaitingComponentInitialization
        }
        Log.d(TAG, "payButtonClicked - setting fragment state $state")
        componentFragmentStateMutable.value = state
    }
}

sealed class PreselectedStoredState {
    object Idle : PreselectedStoredState()
    object ShowStoredPaymentDialog : PreselectedStoredState()
    object AwaitingComponentInitialization : PreselectedStoredState()
    class RequestPayment(val componentState: PaymentComponentState<PaymentMethodDetails>) : PreselectedStoredState()

    override fun toString(): String = this::class.simpleName ?: ""
}
