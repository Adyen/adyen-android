/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel
import com.adyen.checkout.dropin.ui.stored.makeStoredModel
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.AwaitingComponentInitialization
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.Idle
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.PaymentError
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.RequestPayment
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.ShowStoredPaymentDialog

class PreselectedStoredPaymentViewModel(
    storedPaymentMethod: StoredPaymentMethod,
    private val componentRequiresInput: Boolean,
    private val isRemovingEnabled: Boolean
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private val storedPaymentMethodMutableLiveData: MutableLiveData<StoredPaymentMethodModel> = MutableLiveData()
    val storedPaymentLiveData: LiveData<StoredPaymentMethodModel> = storedPaymentMethodMutableLiveData

    private val componentFragmentStateMutable = MutableLiveData<PreselectedStoredState>(Idle)
    val componentFragmentState: LiveData<PreselectedStoredState> = componentFragmentStateMutable

    private var componentState: PaymentComponentState<PaymentMethodDetails>? = null
    private var lastComponentError: ComponentError? = null

    init {
        storedPaymentMethodMutableLiveData.value = makeStoredModel(storedPaymentMethod, isRemovingEnabled)
    }

    fun componentStateChanged(componentState: PaymentComponentState<in PaymentMethodDetails>) {
        val fragmentState = componentFragmentStateMutable.value
        Logger.v(
            TAG,
            "componentStateChanged - componentState.isReady: ${componentState.isReady} - " +
                "fragmentState: $fragmentState"
        )
        this.componentState = componentState
        if (!componentRequiresInput && componentState.isReady && fragmentState is AwaitingComponentInitialization) {
            val state = RequestPayment(componentState)
            Logger.v(TAG, "componentStateChanged - setting fragment state $state")
            componentFragmentStateMutable.value = state
        }
    }

    fun payButtonClicked() {
        val fragmentState = componentFragmentStateMutable.value
        val componentState = componentState
        Logger.v(
            TAG,
            "payButtonClicked - componentState.isReady: ${componentState?.isReady} - " +
                "fragmentState: $fragmentState"
        )
        // check whether an error has occurred already before Pay was clicked
        val componentError = lastComponentError
        val state = when {
            componentRequiresInput -> ShowStoredPaymentDialog
            componentError != null -> PaymentError(componentError)
            componentState?.isReady == true -> RequestPayment(componentState)
            else -> AwaitingComponentInitialization
        }
        Logger.v(TAG, "payButtonClicked - setting fragment state $state")
        componentFragmentStateMutable.value = state
    }

    fun componentErrorOccurred(componentError: ComponentError) {
        lastComponentError = componentError
        val fragmentState = componentFragmentStateMutable.value
        val componentState = componentState
        Logger.v(
            TAG,
            "componentErrorOccurred - componentState.isReady: ${componentState?.isReady} - " +
                "fragmentState: $fragmentState"
        )
        if (fragmentState is AwaitingComponentInitialization) {
            val state = PaymentError(componentError)
            Logger.v(TAG, "componentErrorOccurred - setting fragment state $state")
            componentFragmentStateMutable.value = state
        }
    }
}

sealed class PreselectedStoredState {
    object Idle : PreselectedStoredState()
    object ShowStoredPaymentDialog : PreselectedStoredState()
    object AwaitingComponentInitialization : PreselectedStoredState()
    class RequestPayment(val componentState: PaymentComponentState<PaymentMethodDetails>) : PreselectedStoredState()
    class PaymentError(val componentError: ComponentError) : PreselectedStoredState()

    override fun toString(): String = this::class.simpleName ?: ""
}
