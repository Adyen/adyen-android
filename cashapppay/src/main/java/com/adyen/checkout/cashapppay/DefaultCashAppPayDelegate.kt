/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/5/2023.
 */

package com.adyen.checkout.cashapppay

import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.GrantType
import app.cash.paykit.core.models.sdk.CashAppPayCurrency
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("TooManyFunctions")
internal class DefaultCashAppPayDelegate(
    private val paymentMethod: PaymentMethod,
    private val configuration: CashAppPayConfiguration
) : CashAppPayDelegate {

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    private lateinit var cashAppPay: CashAppPay

    override fun shouldCreateComponentStateOnInit(): Boolean = false

    override suspend fun initialize(outputData: CashAppPayOutputData?, onCashAppPayStateChanged: (CashAppPayState) -> Unit) {
        val cashAppParams = getCashAppParams()
        cashAppPay = initCashAppPay(cashAppParams, onCashAppPayStateChanged)
        if (!requiresInput()) {
            initiateCashAppPayment(cashAppParams, outputData)
        }
    }

    @Suppress("ThrowsCount")
    private fun getCashAppParams(): CashAppParams {
        return CashAppParams(
            clientId = paymentMethod.configuration?.clientId
                ?: throw ComponentException("Cannot launch Cash App Pay, clientId is missing from the payment method object"),
            scopeId = paymentMethod.configuration?.scopeId
                ?: throw ComponentException("Cannot launch Cash App Pay, scopeId is missing from the payment method object"),
            returnUrl = configuration.returnUrl
                ?: throw ComponentException("Cannot launch Cash App Pay, set the returnUrl in your CashAppPayConfiguration.Builder"),
        )
    }

    private fun initCashAppPay(cashAppParams: CashAppParams, onCashAppPayStateChanged: (CashAppPayState) -> Unit): CashAppPay {
        return when (configuration.cashAppPayEnvironment) {
            CashAppPayEnvironment.SANDBOX -> CashAppPayFactory.createSandbox(cashAppParams.clientId)
            CashAppPayEnvironment.PRODUCTION -> CashAppPayFactory.create(cashAppParams.clientId)
        }.apply {
            registerForStateUpdates(DefaultCashAppPayListener(onCashAppPayStateChanged))
        }
    }

    private suspend fun initiateCashAppPayment(cashAppParams: CashAppParams, outputData: CashAppPayOutputData?) {
        val actions = listOfNotNull(
            getOneTimeAction(cashAppParams),
            getOnFileAction(cashAppParams, outputData),
        )

        if (actions.isEmpty()) {
            throw ComponentException("Cannot launch Cash App Pay, you need to either pass an amount or store the shopper account")
        }

        withContext(Dispatchers.IO) {
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

    private fun getOnFileAction(cashAppParams: CashAppParams, outputData: CashAppPayOutputData?): CashAppPayPaymentAction.OnFileAction? {
        val shouldStorePaymentMethod = when {
            // shopper is presented with store switch and selected it
            configuration.showStorePaymentField && outputData?.isStorePaymentSelected == true -> true
            // shopper is not presented with store switch and configuration indicates storing the payment method
            !configuration.showStorePaymentField && configuration.storePaymentMethod -> true
            else -> false
        }

        // we don't create a OnFileAction when storing is not required
        if (!shouldStorePaymentMethod) return null

        return CashAppPayPaymentAction.OnFileAction(
            scopeId = cashAppParams.scopeId,
        )
    }

    @Suppress("ReturnCount")
    override fun cashAppPayStateChanged(newState: CashAppPayState, outputData: CashAppPayOutputData?): CashAppPayStateChangedResult {
        Logger.d(TAG, "CashAppPayState state changed: ${newState::class.simpleName}")
        when (newState) {
            is CashAppPayState.ReadyToAuthorize -> {
                cashAppPay.authorizeCustomerRequest()
            }

            is CashAppPayState.Approved -> {
                Logger.i(TAG, "Cash App Pay authorization request approved")
                outputData?.copy(authorizationData = getCashAppPayAuthorizationData(newState.responseData))?.let {
                    return CashAppPayStateChangedResult.Success(it)
                }
            }

            CashAppPayState.Declined -> {
                Logger.i(TAG, "Cash App Pay authorization request declined")
                return CashAppPayStateChangedResult.Error(ComponentException("Cash App Pay authorization request declined"))
            }

            is CashAppPayState.CashAppPayExceptionState -> {
                return CashAppPayStateChangedResult.Error(ComponentException("Cash App Pay has encountered an error", newState.exception))
            }

            else -> {
                // no ops
            }
        }
        return CashAppPayStateChangedResult.NoOps
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

    override fun showStorePaymentField(): Boolean {
        return configuration.showStorePaymentField
    }

    override fun requiresInput(): Boolean {
        return showStorePaymentField()
    }

    override fun onCleared() {
        cashAppPay.unregisterFromStateUpdates()
    }

    override fun createComponentState(outputData: CashAppPayOutputData?): GenericComponentState<CashAppPayPaymentMethod> {
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

    override suspend fun submit(outputData: CashAppPayOutputData?) {
        val cashAppParams = getCashAppParams()
        if (requiresInput()) {
            initiateCashAppPayment(cashAppParams, outputData)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
