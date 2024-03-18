/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.blik.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.getBlikConfiguration
import com.adyen.checkout.blik.internal.ui.DefaultBlikDelegate
import com.adyen.checkout.blik.internal.ui.StoredBlikDelegate
import com.adyen.checkout.blik.toCheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.provider.StoredPaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
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
class BlikComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        BlikComponent,
        BlikConfiguration,
        BlikComponentState,
        ComponentCallback<BlikComponentState>,
        >,
    StoredPaymentComponentProvider<
        BlikComponent,
        BlikConfiguration,
        BlikComponentState,
        ComponentCallback<BlikComponentState>,
        >,
    SessionPaymentComponentProvider<
        BlikComponent,
        BlikConfiguration,
        BlikComponentState,
        SessionComponentCallback<BlikComponentState>,
        >,
    SessionStoredPaymentComponentProvider<
        BlikComponent,
        BlikConfiguration,
        BlikComponentState,
        SessionComponentCallback<BlikComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BlikComponentState>,
        order: Order?,
        key: String?,
    ): BlikComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getBlikConfiguration(),
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val blikDelegate = DefaultBlikDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            BlikComponent(
                blikDelegate = blikDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BlikComponent::class.java]
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
        configuration: BlikConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BlikComponentState>,
        order: Order?,
        key: String?,
    ): BlikComponent {
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
        componentCallback: ComponentCallback<BlikComponentState>,
        order: Order?,
        key: String?,
    ): BlikComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getBlikConfiguration(),
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val blikDelegate = StoredBlikDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                storedPaymentMethod = storedPaymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            BlikComponent(
                blikDelegate = blikDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, BlikComponent::class.java]
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
        configuration: BlikConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BlikComponentState>,
        order: Order?,
        key: String?,
    ): BlikComponent {
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
        componentCallback: SessionComponentCallback<BlikComponentState>,
        key: String?
    ): BlikComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                componentConfiguration = checkoutConfiguration.getBlikConfiguration(),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val blikDelegate = DefaultBlikDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
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

            val sessionComponentEventHandler = SessionComponentEventHandler<BlikComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            BlikComponent(
                blikDelegate = blikDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BlikComponent::class.java]
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
        configuration: BlikConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<BlikComponentState>,
        key: String?
    ): BlikComponent {
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
        componentCallback: SessionComponentCallback<BlikComponentState>,
        key: String?
    ): BlikComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                componentConfiguration = checkoutConfiguration.getBlikConfiguration(),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val blikDelegate = StoredBlikDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                storedPaymentMethod = storedPaymentMethod,
                order = checkoutSession.order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
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

            val sessionComponentEventHandler =
                SessionComponentEventHandler<BlikComponentState>(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                )

            BlikComponent(
                blikDelegate = blikDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, blikDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, BlikComponent::class.java]
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
        configuration: BlikConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<BlikComponentState>,
        key: String?
    ): BlikComponent {
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
