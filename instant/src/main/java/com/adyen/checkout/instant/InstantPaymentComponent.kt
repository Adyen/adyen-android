package com.adyen.checkout.instant

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.instant.internal.provider.InstantPaymentComponentProvider
import com.adyen.checkout.instant.internal.ui.InstantPaymentDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] used for handling payment methods that do not require any input from the shopper.
 */
class InstantPaymentComponent internal constructor(
    private val instantPaymentDelegate: InstantPaymentDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<InstantComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    @Suppress("ForbiddenComment")
    // FIXME: Using actionHandlingComponent.activeDelegate will crash for QR code actions. This is a workaround for the
    //  actual issue.
    override val delegate: ComponentDelegate get() = genericActionDelegate.delegate

    override val viewFlow: Flow<ComponentViewType?> = genericActionDelegate.viewFlow

    init {
        instantPaymentDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<InstantComponentState>) -> Unit
    ) {
        instantPaymentDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        instantPaymentDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        // no ops
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        instantPaymentDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = InstantPaymentComponentProvider()

        /**
         * These are the payment method types known to be supported by the [InstantPaymentComponent]. There are
         * payment method types that are not listed here that are actually supported. See
         * [InstantPaymentComponentProvider.isPaymentMethodSupported] for more details.
         */
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.DUIT_NOW,
            PaymentMethodTypes.PAY_NOW,
            PaymentMethodTypes.PIX,
            PaymentMethodTypes.PROMPT_PAY,
            PaymentMethodTypes.TWINT,
            PaymentMethodTypes.WECHAT_PAY_SDK,
            PaymentMethodTypes.MULTIBANCO,
        )
    }
}
