/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.internal.ActionHandlingComponent
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.giftcard.internal.provider.GiftCardComponentProvider
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the 'giftcard' payment method.
 */
class GiftCardComponent internal constructor(
    private val giftCardDelegate: GiftCardDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<GiftCardComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ButtonComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        giftCardDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        giftCardDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<GiftCardComponentState>) -> Unit
    ) {
        giftCardDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        giftCardDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun isConfirmationRequired(): Boolean = giftCardDelegate.isConfirmationRequired()

    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit() ?: Logger.e(TAG, "Component is currently not submittable, ignoring.")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? GiftCardDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    /**
     * Pass the [BalanceResult] you get from the call to the /paymentMethods/balance endpoint of the Checkout API to
     * continue the gift card flow. You should make this call in the [GiftCardComponentCallback.onBalanceCheck]
     * callback. Deserialize the response using [BalanceResult.SERIALIZER].
     *
     * @param balanceResult The deserialized response from the /paymentMethods/balance endpoint.
     */
    fun resolveBalanceResult(balanceResult: BalanceResult) {
        (delegate as? GiftCardDelegate)?.resolveBalanceResult(balanceResult)
            ?: Logger.e(TAG, "Payment component is not able to resolve balance result, ignoring.")
    }

    /**
     * Pass the [OrderResponse] you get from the call to the /orders endpoint of the Checkout API to continue the gift
     * card flow. You should make this call in the [GiftCardComponentCallback.onRequestOrder] callback. Deserialize the
     * response using [OrderResponse.SERIALIZER].
     *
     * @param orderResponse The deserialized response from the /orders endpoint.
     */
    fun resolveOrderResponse(orderResponse: OrderResponse) {
        (delegate as? GiftCardDelegate)?.resolveOrderResponse(orderResponse)
            ?: Logger.e(TAG, "Payment component is not able to resolve order response, ignoring.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        giftCardDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = GiftCardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GIFTCARD)
    }
}
