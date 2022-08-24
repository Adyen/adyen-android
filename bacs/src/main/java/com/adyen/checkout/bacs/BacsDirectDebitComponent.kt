/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class BacsDirectDebitComponent(
    savedStateHandle: SavedStateHandle,
    private val bacsDirectDebitDelegate: BacsDirectDebitDelegate,
    configuration: BacsDirectDebitConfiguration
) : BasePaymentComponent<BacsDirectDebitConfiguration, BacsDirectDebitInputData, BacsDirectDebitOutputData,
    BacsDirectDebitComponentState>(savedStateHandle, bacsDirectDebitDelegate, configuration) {

    override val inputData: BacsDirectDebitInputData = BacsDirectDebitInputData()

    init {
        bacsDirectDebitDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        bacsDirectDebitDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: BacsDirectDebitInputData) {
        bacsDirectDebitDelegate.onInputDataChanged(inputData)
    }

    fun setInputMode() {
        inputData.mode = BacsDirectDebitMode.INPUT
        notifyInputDataChanged()
    }

    fun setConfirmationMode() {
        inputData.mode = BacsDirectDebitMode.CONFIRMATION
        notifyInputDataChanged()
    }

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<BacsDirectDebitComponent, BacsDirectDebitConfiguration> =
            BacsComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BACS)
    }
}
