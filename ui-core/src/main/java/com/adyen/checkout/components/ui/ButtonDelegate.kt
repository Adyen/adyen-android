package com.adyen.checkout.components.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.flow.Flow

// TODO docs
interface ButtonDelegate {

    val submitFlow: Flow<PaymentComponentState<out PaymentMethodDetails>>

    fun onSubmit()

    /**
     * TODO: Update docs
     * Tells if the view interaction requires confirmation from the user to start the payment flow.
     * Confirmation usually is obtained by a "Pay" button the user need to press to start processing the payment.
     * If confirmation is not required, it means the view handles input in a way that the user has already expressed the
     * desire to continue.
     *
     * Each type of view always returns the same value, so if the type of view is known, there is no need to check this
     * method.
     *
     * @return If an update from the component attached to this View requires further user confirmation to continue or
     * not.
     */
    fun isConfirmationRequired(): Boolean

    fun shouldShowSubmitButton(): Boolean
}
