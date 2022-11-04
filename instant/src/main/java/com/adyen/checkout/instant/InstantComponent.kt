package com.adyen.checkout.instant

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class InstantComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    override val delegate: InstantDelegate,
    configuration: InstantConfiguration
) : BasePaymentComponent<InstantConfiguration, PaymentComponentState<PaymentMethodDetails>>(
    savedStateHandle,
    delegate,
    configuration
) {

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

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
        val PROVIDER: PaymentComponentProvider<InstantComponent, InstantConfiguration> = InstantComponentProvider()

        // FIXME txVariants
        @JvmField
        val PAYMENT_METHOD_TYPES = emptyArray<String>()
    }
}
