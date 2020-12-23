/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/7/2019.
 */
package com.adyen.checkout.googlepay

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import java.lang.ref.WeakReference

class GooglePayProvider : PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration> {
    override operator fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration
    ): GooglePayComponent {
        val googlePayFactory = viewModelFactory { GooglePayComponent(GenericPaymentMethodDelegate(paymentMethod), configuration) }
        return ViewModelProvider(viewModelStoreOwner, googlePayFactory).get(GooglePayComponent::class.java)
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration,
        callback: ComponentAvailableCallback<GooglePayConfiguration>
    ) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext) != ConnectionResult.SUCCESS) {
            callback.onAvailabilityResult(false, paymentMethod, configuration)
            return
        }
        val callbackWeakReference: WeakReference<ComponentAvailableCallback<GooglePayConfiguration>> =
            WeakReference<ComponentAvailableCallback<GooglePayConfiguration>>(callback)
        val paymentsClient: PaymentsClient = Wallet.getPaymentsClient(applicationContext, GooglePayUtils.createWalletOptions(configuration))
        val readyToPayRequest: IsReadyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(configuration)
        val readyToPayTask: Task<Boolean> = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnCompleteListener { task ->
            callbackWeakReference.get()?.onAvailabilityResult(task.result == true, paymentMethod, configuration)
        }
    }
}
