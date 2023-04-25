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
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.GrantType
import app.cash.paykit.core.models.sdk.CashAppPayCurrency
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
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
    private val paymentMethodDelegate: GenericPaymentMethodDelegate,
    private val configuration: CashAppPayConfiguration
) : BasePaymentComponent<CashAppPayConfiguration, CashAppPayInputData, CashAppPayOutputData, GenericComponentState<CashAppPayPaymentMethod>>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    private val cashAppPay: CashAppPay
    internal val inputData = CashAppPayInputData()

    init {
        val cashAppParams = getCashAppParams()
        cashAppPay = initCashAppPay(cashAppParams)
        if (!isUserInteractionRequired()) {
            initiateCashAppPayment(cashAppParams)
        }
    }

    private fun getCashAppParams(): CashAppParams {
        return CashAppParams(
            clientId = paymentMethodDelegate.paymentMethod.configuration?.clientId
                ?: throw ComponentException("Cannot launch Cash App Pay, clientId is missing from the payment method object"),
            scopeId = paymentMethodDelegate.paymentMethod.configuration?.scopeId
                ?: throw ComponentException("Cannot launch Cash App Pay, scopeId is missing from the payment method object"),
            returnUrl = configuration.returnUrl
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
        val actions = listOfNotNull(
            getOneTimeAction(cashAppParams),
            getOnFileAction(cashAppParams),
        )

        if (actions.isEmpty()) {
            throw ComponentException("Cannot launch Cash App Pay, you need to either pass an amount or store the shopper account")
        }

        viewModelScope.launch(Dispatchers.IO) {
            // must be called from a background thread according to Cash App Pay docs
            cashAppPay.createCustomerRequest(actions, cashAppParams.returnUrl)
        }
    }

    private fun getOneTimeAction(cashAppParams: CashAppParams): CashAppPayPaymentAction.OneTimeAction? {
        val amount = configuration.amount

        // we don't create a OneTimeAction from transactions with no amount
        if (amount.value <= 0) return null

        val cashAppPayCurrency = when (amount.currency) {
            CheckoutCurrency.USD.name -> CashAppPayCurrency.USD
            else -> throw ComponentException("Unsupported currency: ${amount.currency}")
        }

        return CashAppPayPaymentAction.OneTimeAction(
            amount = amount.value,
            currency = cashAppPayCurrency,
            scopeId = cashAppParams.scopeId,
        )
    }

    private fun getOnFileAction(cashAppParams: CashAppParams): CashAppPayPaymentAction.OnFileAction? {
        val isStorePaymentSelected = outputData?.isStorePaymentSelected ?: false

        // we don't create a OneTimeAction from transactions with no amount
        if (!isStorePaymentSelected) return null

        return CashAppPayPaymentAction.OnFileAction(
            scopeId = cashAppParams.scopeId,
        )
    }

    private fun onCashAppPayStateChanged(newState: CashAppPayState) {
        Logger.d(TAG, "CashAppPayState state changed: ${newState::class.simpleName}")
        when (newState) {
            is CashAppPayState.ReadyToAuthorize -> {
                cashAppPay.authorizeCustomerRequest()
            }

            is CashAppPayState.Approved -> {
                Logger.i(TAG, "Cash App Pay authorization request approved")
                val newOutputData = outputData?.copy(
                    authorizationData = getCashAppPayAuthorizationData(newState.responseData)
                ) ?: return
                notifyStateChanged(newOutputData)
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

    private fun getCashAppPayAuthorizationData(responseData: CustomerResponseData): CashAppPayAuthorizationData {
        return CashAppPayAuthorizationData(
            oneTimeData = getCashAppPayOneTimeData(responseData),
            onFileData = getCashAppPayOnFileData(responseData),
        )
    }

    private fun getCashAppPayOneTimeData(responseData: CustomerResponseData): CashAppPayOneTimeData? {
        val grants = responseData.grants.orEmpty()
        val oneTimeGrant = grants.find { it.type == GrantType.ONE_TIME } ?: return null
        return CashAppPayOneTimeData(
            grantId = oneTimeGrant.id,
        )
    }

    private fun getCashAppPayOnFileData(responseData: CustomerResponseData): CashAppPayOnFileData? {
        val grants = responseData.grants.orEmpty()
        val onFileGrant = grants.find { it.type == GrantType.EXTENDED } ?: return null
        return CashAppPayOnFileData(
            grantId = onFileGrant.id,
            cashTag = responseData.customerProfile?.cashTag,
            customerId = responseData.customerProfile?.id,
        )
    }

    internal fun showStorePaymentField(): Boolean {
        return configuration.showStorePaymentField
    }

    internal fun isUserInteractionRequired(): Boolean {
        return showStorePaymentField()
    }

    override fun onCleared() {
        super.onCleared()
        cashAppPay.unregisterFromStateUpdates()
    }

    override fun createComponentState(): GenericComponentState<CashAppPayPaymentMethod> {
        val outputData = outputData
        val oneTimeData = outputData?.authorizationData?.oneTimeData
        val onFileData = outputData?.authorizationData?.onFileData

        val cashAppPayPaymentMethod = CashAppPayPaymentMethod(
            grantId = oneTimeData?.grantId,
            customerId = onFileData?.customerId,
            onFileGrantId = onFileData?.grantId,
            cashtag = onFileData?.cashTag,
        ).apply {
            type = CashAppPayPaymentMethod.PAYMENT_METHOD_TYPE
        }
        val paymentComponentData = PaymentComponentData<CashAppPayPaymentMethod>().apply {
            paymentMethod = cashAppPayPaymentMethod
            setStorePaymentMethod(onFileData != null)
        }
        return GenericComponentState(
            paymentComponentData,
            outputData?.isValid ?: false,
            true,
        )
    }

    override fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData {
        return CashAppPayOutputData(
            isStorePaymentSelected = inputData.isStorePaymentSelected
        )
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    /**
     * Call this to indicate that the shopper has clicked the Pay button and Cash App Pay is ready to authorize the request.
     * You should only call this method when the component requires user interaction, which means when the "Store payment method" switch is shown.
     * You can check this value using [CashAppPayView.isConfirmationRequired].
     */
    fun submit() {
        val cashAppParams = getCashAppParams()
        if (isUserInteractionRequired()) {
            initiateCashAppPayment(cashAppParams)
        }
    }

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
