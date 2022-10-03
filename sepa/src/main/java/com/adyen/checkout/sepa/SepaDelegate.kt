package com.adyen.checkout.sepa

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface SepaDelegate :
    PaymentMethodDelegate<
        SepaConfiguration,
        SepaInputData,
        SepaOutputData,
        PaymentComponentState<SepaPaymentMethod>
        >,
    ViewProvidingDelegate {

    val configuration: SepaConfiguration

    val inputData: SepaInputData

    val outputData: SepaOutputData?

    val outputDataFlow: Flow<SepaOutputData?>

    val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>?>
}
