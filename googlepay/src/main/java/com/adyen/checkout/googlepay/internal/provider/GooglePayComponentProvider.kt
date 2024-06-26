/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/7/2019.
 */
package com.adyen.checkout.googlepay.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.internal.ui.DefaultGooglePayDelegate
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParamsMapper
import com.adyen.checkout.googlepay.internal.util.GooglePayUtils
import com.adyen.checkout.googlepay.toCheckoutConfiguration
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.Wallet
import java.lang.ref.WeakReference

class GooglePayComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        GooglePayComponent,
        GooglePayConfiguration,
        GooglePayComponentState,
        ComponentCallback<GooglePayComponentState>,
        >,
    SessionPaymentComponentProvider<
        GooglePayComponent,
        GooglePayConfiguration,
        GooglePayComponentState,
        SessionComponentCallback<GooglePayComponentState>,
        >,
    PaymentMethodAvailabilityCheck<GooglePayConfiguration> {

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<GooglePayComponentState>,
        order: Order?,
        key: String?,
    ): GooglePayComponent {
        assertSupported(paymentMethod)
        val googlePayFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = GooglePayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                paymentMethod = paymentMethod,
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val paymentsClient =
                Wallet.getPaymentsClient(application, GooglePayUtils.createWalletOptions(componentParams))

            val googlePayDelegate = DefaultGooglePayDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                paymentsClient = paymentsClient,
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            GooglePayComponent(
                googlePayDelegate = googlePayDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, googlePayDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, googlePayFactory)[key, GooglePayComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
                }
            }
    }

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration,
        application: Application,
        componentCallback: ComponentCallback<GooglePayComponentState>,
        order: Order?,
        key: String?,
    ): GooglePayComponent {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            paymentMethod = paymentMethod,
            checkoutConfiguration = configuration.toCheckoutConfiguration(),
            application = application,
            componentCallback = componentCallback,
            order = order,
            key = key,
        )
    }

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<GooglePayComponentState>,
        key: String?
    ): GooglePayComponent {
        assertSupported(paymentMethod)
        val googlePayFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = GooglePayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                paymentMethod = paymentMethod,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val paymentsClient =
                Wallet.getPaymentsClient(application, GooglePayUtils.createWalletOptions(componentParams))

            val googlePayDelegate = DefaultGooglePayDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                paymentsClient = paymentsClient,
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
            )

            val sessionInteractor = SessionInteractor(
                sessionRepository = SessionRepository(
                    sessionService = SessionService(httpClient),
                    clientKey = componentParams.clientKey,
                ),
                sessionModel = sessionSavedStateHandleContainer.getSessionModel(),
                isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
            )

            val sessionComponentEventHandler = SessionComponentEventHandler<GooglePayComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            GooglePayComponent(
                googlePayDelegate = googlePayDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, googlePayDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, googlePayFactory)[key, GooglePayComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
                }
            }
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: GooglePayConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<GooglePayComponentState>,
        key: String?
    ): GooglePayComponent {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            checkoutConfiguration = configuration.toCheckoutConfiguration(),
            application = application,
            componentCallback = componentCallback,
            key = key,
        )
    }

    override fun isAvailable(
        application: Application,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ComponentAvailableCallback
    ) {
        if (
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(application) != ConnectionResult.SUCCESS
        ) {
            callback.onAvailabilityResult(false, paymentMethod)
            return
        }
        val callbackWeakReference = WeakReference(callback)
        val componentParams = GooglePayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = localeProvider.getLocale(application),
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = paymentMethod,
        )

        val paymentsClient = Wallet.getPaymentsClient(application, GooglePayUtils.createWalletOptions(componentParams))
        val readyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(componentParams)
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnSuccessListener { result ->
            callbackWeakReference.get()?.onAvailabilityResult(result == true, paymentMethod)
        }
        readyToPayTask.addOnCanceledListener {
            adyenLog(AdyenLogLevel.ERROR) { "GooglePay readyToPay task is cancelled." }
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
        }
        readyToPayTask.addOnFailureListener {
            adyenLog(AdyenLogLevel.ERROR, it) { "GooglePay readyToPay task is failed." }
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
        }
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

        isAvailable(
            application = applicationContext,
            paymentMethod = paymentMethod,
            checkoutConfiguration = configuration.toCheckoutConfiguration(),
            callback = callback,
        )
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
