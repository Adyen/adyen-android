package com.adyen.checkout.sepa

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UiStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface SepaDelegate :
    PaymentComponentDelegate<PaymentComponentState<SepaPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UiStateDelegate {

    val outputData: SepaOutputData

    val outputDataFlow: Flow<SepaOutputData>

    val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>>

    fun updateInputData(update: SepaInputData.() -> Unit)
}
