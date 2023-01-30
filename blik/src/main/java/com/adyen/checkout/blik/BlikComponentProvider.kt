/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.blik

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultComponentEventHandler
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback
import com.adyen.checkout.sessions.SessionHandler
import com.adyen.checkout.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.interactor.SessionInteractor
import com.adyen.checkout.sessions.provider.SessionStoredPaymentComponentProvider
import com.adyen.checkout.sessions.repository.SessionRepository

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BlikComponentProvider(
    overrideComponentParams: ComponentParams? = null
) : SessionStoredPaymentComponentProvider<BlikComponent, BlikConfiguration, PaymentComponentState<BlikPaymentMethod>> {

    private val componentParamsMapper = ButtonComponentParamsMapper(overrideComponentParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: BlikConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: ComponentCallback<PaymentComponentState<BlikPaymentMethod>>,
        order: Order?,
        key: String?,
    ): BlikComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )

                val blikDelegate = DefaultBlikDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                )

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

                val componentEventHandler = DefaultComponentEventHandler(componentCallback)

                BlikComponent(
                    blikDelegate = blikDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                    componentEventHandler = componentEventHandler,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BlikComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: BlikConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: ComponentCallback<PaymentComponentState<BlikPaymentMethod>>,
        order: Order?,
        key: String?,
    ): BlikComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, storedPaymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )

                val blikDelegate = StoredBlikDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    storedPaymentMethod = storedPaymentMethod,
                    order = order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                )

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

                val componentEventHandler = DefaultComponentEventHandler(componentCallback)

                BlikComponent(
                    blikDelegate = blikDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                    componentEventHandler = componentEventHandler,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, BlikComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: BlikConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: SessionComponentCallback<PaymentComponentState<BlikPaymentMethod>>,
        key: String?
    ): BlikComponent {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )

                val blikDelegate = DefaultBlikDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = checkoutSession.order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                )

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
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
                    isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false
                )

                val sessionHandler = SessionHandler(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                    sessionComponentCallback = componentCallback
                )

                BlikComponent(
                    blikDelegate = blikDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                    componentEventHandler = sessionHandler,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BlikComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: BlikConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: SessionComponentCallback<PaymentComponentState<BlikPaymentMethod>>,
        key: String?
    ): BlikComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, storedPaymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )

                val blikDelegate = StoredBlikDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    storedPaymentMethod = storedPaymentMethod,
                    order = checkoutSession.order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                )

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
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
                    isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false
                )

                val sessionHandler = SessionHandler(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                    sessionComponentCallback = componentCallback
                )

                BlikComponent(
                    blikDelegate = blikDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                    componentEventHandler = sessionHandler,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, BlikComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    private fun assertSupported(storedPaymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(storedPaymentMethod)) {
            throw ComponentException("Unsupported payment method ${storedPaymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BlikComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return BlikComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
