/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param mbWayDelegate [MBWayDelegate]
 * @param configuration [MBWayConfiguration]
 */
class MBWayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    private val mbWayDelegate: MBWayDelegate,
    configuration: MBWayConfiguration
) :
    BasePaymentComponent<MBWayConfiguration, MBWayInputData, MBWayOutputData,
        PaymentComponentState<MBWayPaymentMethod>>(savedStateHandle, paymentMethodDelegate, configuration) {

    init {
        observeOutputData()
        observeComponentState()
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: MBWayInputData) {
        mbWayDelegate.onInputDataChanged(inputData)
    }

    private fun observeOutputData() {
        mbWayDelegate.outputDataFlow.filterNotNull().onEach {
            notifyOutputDataChanged(it)
        }.launchIn(viewModelScope)
    }

    private fun observeComponentState() {
        mbWayDelegate.componentStateFlow.filterNotNull().onEach {
            notifyStateChanged(it)
        }.launchIn(viewModelScope)
    }

    fun getSupportedCountries(): List<String> = SUPPORTED_COUNTRIES

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<MBWayComponent, MBWayConfiguration> = MBWayComponentProvider()

        private val TAG = LogUtil.getTag()

        private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.MB_WAY)
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"
        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
