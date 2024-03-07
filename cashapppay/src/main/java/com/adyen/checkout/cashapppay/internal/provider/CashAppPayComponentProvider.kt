/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import app.cash.paykit.core.CashAppPayFactory
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.internal.ui.CashAppPayDelegate
import com.adyen.checkout.cashapppay.internal.ui.DefaultCashAppPayDelegate
import com.adyen.checkout.cashapppay.internal.ui.StoredCashAppPayDelegate
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParams
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParamsMapper
import com.adyen.checkout.cashapppay.toCheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepositoryData
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.provider.StoredPaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.provider.SessionStoredPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

@Suppress("TooManyFunctions")
class CashAppPayComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        CashAppPayComponent,
        CashAppPayConfiguration,
        CashAppPayComponentState,
        ComponentCallback<CashAppPayComponentState>,
        >,
    StoredPaymentComponentProvider<
        CashAppPayComponent,
        CashAppPayConfiguration,
        CashAppPayComponentState,
        ComponentCallback<CashAppPayComponentState>,
        >,
    SessionPaymentComponentProvider<
        CashAppPayComponent,
        CashAppPayConfiguration,
        CashAppPayComponentState,
        SessionComponentCallback<CashAppPayComponentState>,
        >,
    SessionStoredPaymentComponentProvider<
        CashAppPayComponent,
        CashAppPayConfiguration,
        CashAppPayComponentState,
        SessionComponentCallback<CashAppPayComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CashAppPayComponentState>,
        order: Order?,
        key: String?
    ): CashAppPayComponent {
        assertSupported(paymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                paymentMethod = paymentMethod,
                context = application,
            )

            val analyticsRepository = analyticsRepository ?: DefaultAnalyticsRepository(
                analyticsRepositoryData = AnalyticsRepositoryData(
                    application = application,
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                ),
                analyticsService = AnalyticsService(
                    HttpClientFactory.getAnalyticsHttpClient(componentParams.environment),
                ),
                analyticsMapper = AnalyticsMapper(),
            )

            val cashAppPayDelegate = DefaultCashAppPayDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                analyticsRepository = analyticsRepository,
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                componentParams = componentParams,
                cashAppPayFactory = CashAppPayFactory,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = cashAppPayDelegate,
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, CashAppPayComponent::class.java]
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
        paymentMethod: PaymentMethod,
        configuration: CashAppPayConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CashAppPayComponentState>,
        order: Order?,
        key: String?
    ): CashAppPayComponent {
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

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CashAppPayComponentState>,
        order: Order?,
        key: String?
    ): CashAppPayComponent {
        assertSupported(storedPaymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                storedPaymentMethod = storedPaymentMethod,
                context = application,
            )

            val analyticsRepository = analyticsRepository ?: DefaultAnalyticsRepository(
                analyticsRepositoryData = AnalyticsRepositoryData(
                    application = application,
                    componentParams = componentParams,
                    storedPaymentMethod = storedPaymentMethod,
                ),
                analyticsService = AnalyticsService(
                    HttpClientFactory.getAnalyticsHttpClient(componentParams.environment),
                ),
                analyticsMapper = AnalyticsMapper(),
            )

            val cashAppPayDelegate = StoredCashAppPayDelegate(
                analyticsRepository = analyticsRepository,
                observerRepository = PaymentObserverRepository(),
                paymentMethod = storedPaymentMethod,
                order = order,
                componentParams = componentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = cashAppPayDelegate,
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, CashAppPayComponent::class.java]
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
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CashAppPayComponentState>,
        order: Order?,
        key: String?
    ): CashAppPayComponent {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            storedPaymentMethod = storedPaymentMethod,
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
        componentCallback: SessionComponentCallback<CashAppPayComponentState>,
        key: String?
    ): CashAppPayComponent {
        assertSupported(paymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                paymentMethod = paymentMethod,
                context = application,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsRepository = analyticsRepository ?: DefaultAnalyticsRepository(
                analyticsRepositoryData = AnalyticsRepositoryData(
                    application = application,
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    sessionId = checkoutSession.sessionSetupResponse.id,
                ),
                analyticsService = AnalyticsService(
                    HttpClientFactory.getAnalyticsHttpClient(componentParams.environment),
                ),
                analyticsMapper = AnalyticsMapper(),
            )

            val cashAppPayDelegate = DefaultCashAppPayDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                analyticsRepository = analyticsRepository,
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
                cashAppPayFactory = CashAppPayFactory,
            )

            val sessionComponentEventHandler = createSessionComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
                httpClient = httpClient,
                componentParams = componentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = cashAppPayDelegate,
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, CashAppPayComponent::class.java]
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
        configuration: CashAppPayConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CashAppPayComponentState>,
        key: String?
    ): CashAppPayComponent {
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

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CashAppPayComponentState>,
        key: String?
    ): CashAppPayComponent {
        assertSupported(storedPaymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                storedPaymentMethod = storedPaymentMethod,
                context = application,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsRepository = analyticsRepository ?: DefaultAnalyticsRepository(
                analyticsRepositoryData = AnalyticsRepositoryData(
                    application = application,
                    componentParams = componentParams,
                    storedPaymentMethod = storedPaymentMethod,
                    sessionId = checkoutSession.sessionSetupResponse.id,
                ),
                analyticsService = AnalyticsService(
                    HttpClientFactory.getAnalyticsHttpClient(componentParams.environment),
                ),
                analyticsMapper = AnalyticsMapper(),
            )

            val cashAppPayDelegate = StoredCashAppPayDelegate(
                analyticsRepository = analyticsRepository,
                observerRepository = PaymentObserverRepository(),
                paymentMethod = storedPaymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
            )

            val sessionComponentEventHandler = createSessionComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
                httpClient = httpClient,
                componentParams = componentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = cashAppPayDelegate,
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, CashAppPayComponent::class.java]
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
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CashAppPayConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CashAppPayComponentState>,
        key: String?
    ): CashAppPayComponent {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            checkoutSession = checkoutSession,
            storedPaymentMethod = storedPaymentMethod,
            checkoutConfiguration = configuration.toCheckoutConfiguration(),
            application = application,
            componentCallback = componentCallback,
            key = key,
        )
    }

    private fun createComponent(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
        delegate: CashAppPayDelegate,
        componentEventHandler: ComponentEventHandler<CashAppPayComponentState>,
    ): CashAppPayComponent {
        val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            application = application,
        )

        return CashAppPayComponent(
            cashAppPayDelegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, delegate),
            componentEventHandler = componentEventHandler,
        )
    }

    private fun createSessionComponentEventHandler(
        savedStateHandle: SavedStateHandle,
        checkoutSession: CheckoutSession,
        httpClient: HttpClient,
        componentParams: CashAppPayComponentParams,
    ): SessionComponentEventHandler<CashAppPayComponentState> {
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

        return SessionComponentEventHandler(
            sessionInteractor = sessionInteractor,
            sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
        )
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    private fun assertSupported(paymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return CashAppPayComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return CashAppPayComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
