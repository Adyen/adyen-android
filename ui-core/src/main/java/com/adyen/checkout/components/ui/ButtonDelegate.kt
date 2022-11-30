package com.adyen.checkout.components.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.flow.Flow

// TODO docs
interface ButtonDelegate {

    val submitFlow: Flow<PaymentComponentState<out PaymentMethodDetails>>

    fun onSubmit()
}
