/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/7/2019.
 */
package com.adyen.checkout.googlepay

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.googlepay.model.GooglePayParams
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import java.lang.ref.WeakReference

private val TAG = LogUtil.getTag()

class GooglePayComponentProvider :
    PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration>,
    PaymentMethodAvailabilityCheck<GooglePayConfiguration> {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration,
        defaultArgs: Bundle?
    ): GooglePayComponent {
        val googlePayFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            GooglePayComponent(
                savedStateHandle = savedStateHandle,
                googlePayDelegate = DefaultGooglePayDelegate(paymentMethod, configuration),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, googlePayFactory).get(GooglePayComponent::class.java)
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration?,
        callback: ComponentAvailableCallback<GooglePayConfiguration>
    ) {
        if (configuration == null) {
            throw CheckoutException("GooglePayConfiguration cannot be null")
        }
        if (
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(applicationContext) != ConnectionResult.SUCCESS
        ) {
            callback.onAvailabilityResult(false, paymentMethod, configuration)
            return
        }
        val callbackWeakReference: WeakReference<ComponentAvailableCallback<GooglePayConfiguration>> =
            WeakReference<ComponentAvailableCallback<GooglePayConfiguration>>(callback)
        val serverGatewayMerchantId = paymentMethod.configuration?.gatewayMerchantId
        val params = GooglePayParams(configuration, serverGatewayMerchantId, paymentMethod.brands)
        val paymentsClient: PaymentsClient =
            Wallet.getPaymentsClient(applicationContext, GooglePayUtils.createWalletOptions(params))
        val readyToPayRequest: IsReadyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(params)
        val readyToPayTask: Task<Boolean> = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnSuccessListener { result ->
            callbackWeakReference.get()?.onAvailabilityResult(result == true, paymentMethod, configuration)
        }
        readyToPayTask.addOnCanceledListener {
            Logger.e(TAG, "GooglePay readyToPay task is cancelled.")
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod, configuration)
        }
        readyToPayTask.addOnFailureListener {
            Logger.e(TAG, "GooglePay readyToPay task is failed.", it)
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod, configuration)
        }
    }
}
