/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 6/3/2023.
 */

package com.adyen.checkout.cashapppay

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paykit.core.CashAppPayState
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import kotlinx.coroutines.launch

class CashAppPayComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    private val cashAppPayDelegate: CashAppPayDelegate,
    configuration: CashAppPayConfiguration
) : BasePaymentComponent<CashAppPayConfiguration, CashAppPayInputData, CashAppPayOutputData, GenericComponentState<CashAppPayPaymentMethod>>(
    savedStateHandle,
    cashAppPayDelegate,
    configuration
) {

    internal val inputData = CashAppPayInputData()

    init {
        if (cashAppPayDelegate.shouldCreateComponentStateOnInit()) {
            inputDataChanged(inputData)
        }
        viewModelScope.launch {
            cashAppPayDelegate.initialize(outputData, ::onCashAppPayStateChanged)
        }
    }

    private fun onCashAppPayStateChanged(newState: CashAppPayState) {
        when (val result = cashAppPayDelegate.cashAppPayStateChanged(newState, outputData)) {
            is CashAppPayStateChangedResult.Success -> {
                notifyStateChanged(result.outputData)
            }

            is CashAppPayStateChangedResult.Error -> {
                notifyException(result.componentException)
            }

            is CashAppPayStateChangedResult.NoOps -> {
                // no ops
            }
        }
    }

    internal fun showStorePaymentField(): Boolean {
        return cashAppPayDelegate.showStorePaymentField()
    }

    override fun onCleared() {
        super.onCleared()
        cashAppPayDelegate.onCleared()
    }

    override fun createComponentState(): GenericComponentState<CashAppPayPaymentMethod> {
        return cashAppPayDelegate.createComponentState(outputData)
    }

    override fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData {
        return cashAppPayDelegate.onInputDataChanged(inputData)
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    override fun requiresInput(): Boolean {
        return cashAppPayDelegate.requiresInput()
    }

    /**
     * Call this to indicate that the shopper has clicked the Pay button and Cash App Pay is ready to authorize the request.
     * You should only call this method when the component requires user interaction, which means when the "Store payment method" switch is shown.
     * You can check this value using [requiresInput].
     */
    fun submit() {
        viewModelScope.launch {
            cashAppPayDelegate.submit(outputData)
        }
    }

    companion object {
        @JvmStatic
        val PROVIDER: StoredPaymentComponentProvider<CashAppPayComponent, CashAppPayConfiguration> = CashAppPayComponentProvider()

        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.CASH_APP_PAY)

        /**
         * Returns the suggested value to be used as the `returnUrl` value in the /payments call and in the [CashAppPayConfiguration].
         *
         * @param context The context provides the package name which constitutes part of the ReturnUrl
         * @return The suggested `returnUrl` to be used. Consists of "adyencheckout://" + App package name.
         */
        fun getReturnUrl(context: Context): String {
            return REDIRECT_RESULT_SCHEME + context.packageName
        }

        private val TAG = LogUtil.getTag()

        private const val REDIRECT_RESULT_SCHEME = BuildConfig.checkoutRedirectScheme + "://"
    }
}
