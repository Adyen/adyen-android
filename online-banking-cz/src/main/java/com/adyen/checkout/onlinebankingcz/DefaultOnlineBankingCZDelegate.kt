/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.Context
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultOnlineBankingCZDelegate(
    private val paymentMethod: PaymentMethod,
    private val paymentMethodFactory: () -> OnlineBankingCZPaymentMethod
) : OnlineBankingDelegate<OnlineBankingCZPaymentMethod> {

    private val _outputDataFlow = MutableStateFlow<OnlineBankingOutputData?>(null)
    override val outputDataFlow: Flow<OnlineBankingOutputData?> get() = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<OnlineBankingCZPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<OnlineBankingCZPaymentMethod>?> = _componentStateFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    override fun getIssuers(): List<OnlineBankingModel> =
        paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onInputDataChanged(inputData: OnlineBankingInputData) {
        val outputData = OnlineBankingOutputData(inputData.selectedIssuer)

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    override fun createComponentState(outputData: OnlineBankingOutputData) {
        val issuerListPaymentMethod = paymentMethodFactory()
        issuerListPaymentMethod.type = getPaymentMethodType()
        issuerListPaymentMethod.issuer = outputData.selectedIssuer?.id ?: ""

        val paymentComponentData = PaymentComponentData(paymentMethod = issuerListPaymentMethod)

        val state = PaymentComponentState(paymentComponentData, outputData.isValid, true)
        _componentStateFlow.tryEmit(state)
    }

    companion object {
        private const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_cs.pdf"
    }

    override fun openPdf(context: Context) {
        val url = TERMS_CONDITIONS_URL
        try {
            PdfOpener.open(context, url)
        } catch (e: CheckoutException) {
            _exceptionFlow.tryEmit(e)
        }
    }
}
