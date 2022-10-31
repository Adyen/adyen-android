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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.ActivityResultHandlingComponent
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.googlepay.GooglePayComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GooglePayComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: GooglePayDelegate,
    configuration: GooglePayConfiguration
) :
    BasePaymentComponent<GooglePayConfiguration, GooglePayComponentState>(
        savedStateHandle,
        delegate,
        configuration
    ),
    ActivityResultHandlingComponent {

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<GooglePayComponentState>) -> Unit
    ) {
        delegate.componentStateFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(PaymentComponentEvent.StateChanged(it)) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(PaymentComponentEvent.Error(ComponentError(it))) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    /**
     * Start the GooglePay screen which will return the result to the provided Activity.
     *
     * @param activity    The activity to start the screen and later receive the result.
     * @param requestCode The code that will be returned on the [Activity.onActivityResult]
     */
    fun startGooglePayScreen(activity: Activity, requestCode: Int) {
        delegate.startGooglePayScreen(activity, requestCode)
    }

    /**
     * Handle the result from the GooglePay screen that was started by [.startGooglePayScreen].
     *
     * @param resultCode The result code from the [Activity.onActivityResult]
     * @param data       The data intent from the [Activity.onActivityResult]
     */
    override fun handleActivityResult(resultCode: Int, data: Intent?) {
        delegate.handleActivityResult(resultCode, data)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration> =
            GooglePayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
