package com.adyen.checkout.sepa

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import kotlinx.coroutines.flow.Flow

interface SepaDelegate :
    PaymentMethodDelegate<
        SepaConfiguration,
        SepaInputData,
        SepaOutputData,
        PaymentComponentState<SepaPaymentMethod>
        > {

    val outputDataFlow: Flow<SepaOutputData?>

    val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>?>
}
