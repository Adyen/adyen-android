/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OnlineBankingCZComponent(
    savedStateHandle: SavedStateHandle,
    private val delegate: DefaultOnlineBankingCZDelegate,
    configuration: OnlineBankingConfiguration
) : BasePaymentComponent<
    OnlineBankingConfiguration,
    OnlineBankingInputData,
    OnlineBankingOutputData,
    PaymentComponentState<OnlineBankingCZPaymentMethod>
    >(savedStateHandle, delegate, configuration) {

    override val inputData: OnlineBankingInputData = OnlineBankingInputData()

    val issuers: List<OnlineBankingModel>
        get() = delegate.getIssuers()

    init {
        delegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        delegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .filterNotNull()
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun onInputDataChanged(inputData: OnlineBankingInputData) {
        delegate.onInputDataChanged(inputData)
    }

    fun openTermsAndConditionsPdf(context: Context) {
        delegate.openPdf(context)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingCZComponent, OnlineBankingConfiguration> =
            OnlineBankingCZComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_CZ)
    }
}
