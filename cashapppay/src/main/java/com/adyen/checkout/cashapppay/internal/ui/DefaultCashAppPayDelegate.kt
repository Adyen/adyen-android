package com.adyen.checkout.cashapppay.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParams
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

internal class DefaultCashAppPayDelegate(
    private val analyticsRepository: AnalyticsRepository,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: CashAppPayComponentParams,
) : CashAppPayDelegate {

    override val viewFlow: Flow<ComponentViewType?>
        get() = TODO("Not yet implemented")

    override val submitFlow: Flow<CashAppPayComponentState>
        get() = TODO("Not yet implemented")

    override fun getPaymentMethodType(): String {
        TODO("Not yet implemented")
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<CashAppPayComponentState>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeObserver() {
        TODO("Not yet implemented")
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        TODO("Not yet implemented")
    }

    override fun onCleared() {
        TODO("Not yet implemented")
    }
}
