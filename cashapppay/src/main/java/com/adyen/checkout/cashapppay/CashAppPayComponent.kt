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
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.models.sdk.CashAppPayCurrency
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.paymentmethods.Configuration
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CashAppPayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: CashAppPayConfiguration
) : BasePaymentComponent<CashAppPayConfiguration, CashAppPayInputData, CashAppPayOutputData, GenericComponentState<CashAppPayPaymentMethod>>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    private val cashAppPay: CashAppPay

    init {
        val cashAppParams = getCashAppParams(paymentMethodDelegate.paymentMethod.configuration, configuration)
        cashAppPay = initCashAppPay(cashAppParams)
        initiateCashAppPayment(cashAppParams)
    }

    private fun getCashAppParams(paymentMethodConfiguration: Configuration?, cashAppPayConfiguration: CashAppPayConfiguration): CashAppParams {
        return CashAppParams(
            clientId = paymentMethodConfiguration?.clientId
                ?: throw ComponentException("Cannot launch Cash App Pay, clientId is missing from the payment method object"),
            scopeId = paymentMethodConfiguration.scopeId
                ?: throw ComponentException("Cannot launch Cash App Pay, scopeId is missing from the payment method object"),
            returnUrl = cashAppPayConfiguration.returnUrl
                ?: throw ComponentException("Cannot launch Cash App Pay, set the returnUrl in your CashAppPayConfiguration.Builder"),
        )
    }

    private fun initCashAppPay(cashAppParams: CashAppParams): CashAppPay {
        return when (configuration.cashAppPayEnvironment) {
            CashAppPayEnvironment.SANDBOX -> CashAppPayFactory.createSandbox(cashAppParams.clientId)
            CashAppPayEnvironment.PRODUCTION -> CashAppPayFactory.create(cashAppParams.clientId)
        }.apply {
            registerForStateUpdates(DefaultCashAppPayListener(::onCashAppPayStateChanged))
        }
    }

    private fun initiateCashAppPayment(cashAppParams: CashAppParams) {
        val amount = configuration.amount

        val cashAppPayCurrency = when (amount.currency) {
            CheckoutCurrency.USD.name -> CashAppPayCurrency.USD
            else -> throw ComponentException("Unsupported currency: ${amount.currency}")
        }

        val request = CashAppPayPaymentAction.OneTimeAction(
            amount = amount.value,
            currency = cashAppPayCurrency,
            scopeId = cashAppParams.scopeId,
        )
        viewModelScope.launch(Dispatchers.IO) {
            // must be called from a background thread according to Cash App Pay docs
            cashAppPay.createCustomerRequest(request, cashAppParams.returnUrl)
        }
    }

    private fun onCashAppPayStateChanged(newState: CashAppPayState) {
        Logger.d(TAG, "CashAppPayState state changed: ${newState::class.simpleName}")
        when (newState) {
            is CashAppPayState.ReadyToAuthorize -> {
                cashAppPay.authorizeCustomerRequest()
            }
            is CashAppPayState.Approved -> {
                Logger.i(TAG, "Cash App Pay authorization request approved")
                notifyStateChanged(CashAppPayOutputData(grantId = newState.responseData.grants?.firstOrNull()?.id))
            }
            CashAppPayState.Declined -> {
                Logger.i(TAG, "Cash App Pay authorization request declined")
                notifyException(ComponentException("Cash App Pay authorization request declined"))
            }
            is CashAppPayState.CashAppPayExceptionState -> {
                notifyException(ComponentException("Cash App Pay has encountered an error", newState.exception))
            }
            else -> {
                // no ops
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cashAppPay.unregisterFromStateUpdates()
    }

    override fun createComponentState(): GenericComponentState<CashAppPayPaymentMethod> {
        val outputData = outputData
        val cashAppPayPaymentMethod = CashAppPayPaymentMethod().apply {
            type = CashAppPayPaymentMethod.PAYMENT_METHOD_TYPE
            grantId = outputData?.grantId
        }
        val paymentComponentData = PaymentComponentData<CashAppPayPaymentMethod>().apply {
            paymentMethod = cashAppPayPaymentMethod
        }
        return GenericComponentState(
            paymentComponentData,
            outputData?.isValid ?: false,
            true,
        )
    }

    override fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData {
        return CashAppPayOutputData()
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<CashAppPayComponent, CashAppPayConfiguration> =
            GenericPaymentComponentProvider(CashAppPayComponent::class.java)

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
