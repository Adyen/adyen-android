/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */
package com.adyen.checkout.googlepay.old

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ActivityResultHandlingComponent
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.googlepay.old.internal.provider.GooglePayComponentProvider
import com.adyen.checkout.googlepay.old.internal.ui.DefaultGooglePayDelegate
import com.adyen.checkout.googlepay.old.internal.ui.GooglePayDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.old.internal.util.mergeViewFlows
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.GOOGLE_PAY] and [PaymentMethodTypes.GOOGLE_PAY_LEGACY]
 * payment methods.
 */
class GooglePayComponent internal constructor(
    private val googlePayDelegate: GooglePayDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<GooglePayComponentState>,
) : ViewModel(),
    PaymentComponent,
    ActivityResultHandlingComponent,
    ButtonComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        googlePayDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        googlePayDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
        componentEventHandler.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<GooglePayComponentState>) -> Unit
    ) {
        googlePayDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    internal fun removeObserver() {
        googlePayDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    /**
     * Start the GooglePay screen which will return the result to the provided Activity.
     *
     * @param activity    The activity to start the screen and later receive the result.
     * @param requestCode The code that will be returned on the [Activity.onActivityResult]
     */
    @Deprecated("Deprecated in favor of submit()", ReplaceWith("submit()"))
    fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        @Suppress("DEPRECATION")
        googlePayDelegate.startGooglePayScreen(activity, requestCode)
    }

    /**
     * Start the GooglePay screen.
     */
    override fun submit() {
        (delegate as? ButtonDelegate)?.onSubmit()
            ?: adyenLog(AdyenLogLevel.ERROR) { "Component is currently not submittable, ignoring." }
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? DefaultGooglePayDelegate)?.setInteractionBlocked(isInteractionBlocked)
            ?: adyenLog(AdyenLogLevel.ERROR) { "Payment component is not interactable, ignoring." }
    }

    override fun isConfirmationRequired(): Boolean = (delegate as? ButtonDelegate)?.isConfirmationRequired() ?: false

    /**
     * Returns some of the parameters required to initialize the [Google Pay button](https://docs.adyen.com/payment-methods/google-pay/android-component/#2-show-the-google-pay-button).
     */
    @Suppress("MaxLineLength")
    fun getGooglePayButtonParameters(): GooglePayButtonParameters {
        return googlePayDelegate.getGooglePayButtonParameters()
    }

    /**
     * Handle the result from the GooglePay screen that was started by [.startGooglePayScreen].
     *
     * @param resultCode The result code from the [Activity.onActivityResult]
     * @param data       The data intent from the [Activity.onActivityResult]
     */
    @Deprecated("When using submit() this method is no longer needed.", ReplaceWith(""))
    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        googlePayDelegate.handleActivityResult(resultCode, data)
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        googlePayDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER = GooglePayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
