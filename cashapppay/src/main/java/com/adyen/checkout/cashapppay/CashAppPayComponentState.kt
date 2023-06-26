package com.adyen.checkout.cashapppay

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod

/**
 * Represents the state of [CashAppPayComponent]
 */
data class CashAppPayComponentState(
    override val data: PaymentComponentData<CashAppPayPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
) : PaymentComponentState<CashAppPayPaymentMethod>
