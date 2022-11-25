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
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.Wallet
import java.lang.ref.WeakReference

private val TAG = LogUtil.getTag()

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GooglePayComponentProvider(
    parentConfiguration: Configuration? = null,
    isCreatedByDropIn: Boolean = false,
) : PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration>,
    PaymentMethodAvailabilityCheck<GooglePayConfiguration> {

    private val componentParamsMapper = GooglePayComponentParamsMapper(parentConfiguration, isCreatedByDropIn)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): GooglePayComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(configuration, paymentMethod)
        val googlePayFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            GooglePayComponent(
                savedStateHandle = savedStateHandle,
                delegate = DefaultGooglePayDelegate(
                    PaymentObserverRepository(),
                    paymentMethod,
                    componentParams
                ),
                configuration = configuration,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, googlePayFactory)[key, GooglePayComponent::class.java]
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
        val callbackWeakReference = WeakReference(callback)
        val componentParams = componentParamsMapper.mapToParams(configuration, paymentMethod)
        val paymentsClient = Wallet.getPaymentsClient(
            applicationContext,
            GooglePayUtils.createWalletOptions(componentParams)
        )
        val readyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(componentParams)
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
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

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return GooglePayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
