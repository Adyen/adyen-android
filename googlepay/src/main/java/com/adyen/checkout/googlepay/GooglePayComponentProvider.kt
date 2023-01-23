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
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionComponentProvider
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.HttpClientFactory
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
    overrideComponentParams: ComponentParams? = null,
) : PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration>,
    PaymentMethodAvailabilityCheck<GooglePayConfiguration> {

    private val componentParamsMapper = GooglePayComponentParamsMapper(overrideComponentParams)

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
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val googlePayDelegate = DefaultGooglePayDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                componentParams = componentParams,
                analyticsRepository = analyticsRepository,
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            GooglePayComponent(
                googlePayDelegate = googlePayDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(
                    savedStateHandle,
                    genericActionDelegate,
                    googlePayDelegate
                ),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, googlePayFactory)[key, GooglePayComponent::class.java]
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration?,
        callback: ComponentAvailableCallback
    ) {
        if (configuration == null) {
            throw CheckoutException("GooglePayConfiguration cannot be null")
        }
        if (
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(applicationContext) != ConnectionResult.SUCCESS
        ) {
            callback.onAvailabilityResult(false, paymentMethod)
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
            callbackWeakReference.get()?.onAvailabilityResult(result == true, paymentMethod)
        }
        readyToPayTask.addOnCanceledListener {
            Logger.e(TAG, "GooglePay readyToPay task is cancelled.")
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
        }
        readyToPayTask.addOnFailureListener {
            Logger.e(TAG, "GooglePay readyToPay task is failed.", it)
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
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
