package com.adyen.checkout.instant

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.instant.InstantPaymentComponent.Companion.PROVIDER

/**
 * Payment component used for handling payment methods that do not require any input from the shopper.
 *
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class InstantPaymentComponent internal constructor(
    override val delegate: InstantPaymentDelegate,
) : ViewModel(),
    PaymentComponent<PaymentComponentState<PaymentMethodDetails>> {

    init {
        delegate.initialize(viewModelScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<PaymentMethodDetails>>) -> Unit
    ) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    override fun removeObserver() {
        delegate.removeObserver()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<InstantPaymentComponent, InstantPaymentConfiguration> =
            InstantPaymentComponentProvider()
    }
}
