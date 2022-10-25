/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcore

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultOnlineBankingDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    private val pdfOpener: PdfOpener,
    private val paymentMethod: PaymentMethod,
    override val configuration: Configuration,
    private val termsAndConditionsUrl: String,
    private val paymentMethodFactory: () -> IssuerListPaymentMethodT
) : OnlineBankingDelegate<IssuerListPaymentMethodT> {

    private val inputData: OnlineBankingInputData = OnlineBankingInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<OnlineBankingOutputData> get() = _outputDataFlow

    override val outputData: OnlineBankingOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>> = _componentStateFlow

    private val _exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(OnlineBankingComponentViewType)

    init {
        val outputData = OnlineBankingOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    override fun getIssuers(): List<OnlineBankingModel> =
        paymentMethod.issuers?.mapToModel() ?: paymentMethod.details.getLegacyIssuers()

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: OnlineBankingInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = OnlineBankingOutputData(inputData.selectedIssuer)

    @VisibleForTesting
    internal fun updateComponentState(outputData: OnlineBankingOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: OnlineBankingOutputData = this.outputData
    ): PaymentComponentState<IssuerListPaymentMethodT> {
        val issuerListPaymentMethod = paymentMethodFactory()
        issuerListPaymentMethod.type = getPaymentMethodType()
        issuerListPaymentMethod.issuer = outputData.selectedIssuer?.id

        val paymentComponentData = PaymentComponentData(paymentMethod = issuerListPaymentMethod)

        return PaymentComponentState(paymentComponentData, outputData.isValid, true)
    }

    override fun openTermsAndConditions(context: Context) {
        try {
            pdfOpener.open(context, termsAndConditionsUrl)
        } catch (e: IllegalStateException) {
            _exceptionChannel.trySend(CheckoutException(e.message ?: "", e.cause))
        }
    }

    override fun getViewProvider(): ViewProvider = OnlineBankingViewProvider
}
