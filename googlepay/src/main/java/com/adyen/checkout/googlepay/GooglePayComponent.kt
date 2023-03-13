/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
package com.adyen.checkout.googlepay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.internal.ActionHandlingComponent
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ActivityResultHandlingComponent
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.toActionCallback
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.googlepay.GooglePayComponent.Companion.PROVIDER
import com.adyen.checkout.googlepay.internal.provider.GooglePayComponentProvider
import com.adyen.checkout.googlepay.internal.ui.GooglePayDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GooglePayComponent internal constructor(
    private val googlePayDelegate: GooglePayDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<GooglePayComponentState>,
) : ViewModel(),
    PaymentComponent,
    ActivityResultHandlingComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = genericActionDelegate.viewFlow

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
    fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        googlePayDelegate.startGooglePayScreen(activity, requestCode)
    }

    /**
     * Handle the result from the GooglePay screen that was started by [.startGooglePayScreen].
     *
     * @param resultCode The result code from the [Activity.onActivityResult]
     * @param data       The data intent from the [Activity.onActivityResult]
     */
    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        googlePayDelegate.handleActivityResult(resultCode, data)
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        Logger.w(TAG, "Interaction with GooglePayComponent can't be blocked")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        googlePayDelegate.onCleared()
        genericActionDelegate.onCleared()
        componentEventHandler.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER = GooglePayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
