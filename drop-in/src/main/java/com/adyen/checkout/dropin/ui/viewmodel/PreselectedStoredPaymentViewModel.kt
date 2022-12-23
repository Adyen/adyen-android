/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/12/2020.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel
import com.adyen.checkout.dropin.ui.stored.mapStoredModel
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.Idle
import com.adyen.checkout.dropin.ui.viewmodel.PreselectedStoredState.ShowStoredPaymentDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class PreselectedStoredPaymentViewModel(
    storedPaymentMethod: StoredPaymentMethod,
    private val componentRequiresInput: Boolean,
    dropInConfiguration: DropInConfiguration,
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private val storedPaymentMethodMutableLiveData = MutableStateFlow(
        storedPaymentMethod.mapStoredModel(
            dropInConfiguration.isRemovingStoredPaymentMethodsEnabled,
            dropInConfiguration.environment,
        )
    )
    val storedPaymentLiveData: Flow<StoredPaymentMethodModel> = storedPaymentMethodMutableLiveData

    private val componentFragmentStateMutable = MutableStateFlow<PreselectedStoredState>(Idle)
    val componentFragmentState: Flow<PreselectedStoredState> = componentFragmentStateMutable

    fun payButtonClicked() {
        if (componentRequiresInput) {
            componentFragmentStateMutable.tryEmit(ShowStoredPaymentDialog)
            return
        }
        componentFragmentStateMutable.tryEmit(PreselectedStoredState.Submit)
    }
}

sealed class PreselectedStoredState {
    object Idle : PreselectedStoredState()
    object ShowStoredPaymentDialog : PreselectedStoredState()
    object Submit : PreselectedStoredState()

    override fun toString(): String = this::class.simpleName ?: ""
}
