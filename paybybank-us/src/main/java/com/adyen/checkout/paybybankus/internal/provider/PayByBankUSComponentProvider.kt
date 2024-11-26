/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
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
import com.adyen.checkout.paybybankus.PayByBankUSComponent
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.PayByBankUSConfiguration
import com.adyen.checkout.paybybankus.getPayByBankUSConfiguration
import com.adyen.checkout.paybybankus.internal.DefaultPayByBankUSDelegate
import com.adyen.checkout.paybybankus.internal.StoredPayByBankUSDelegate
import com.adyen.checkout.paybybankus.toCheckoutConfiguration
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
class PayByBankUSComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        PayByBankUSComponent,
        PayByBankUSConfiguration,
        PayByBankUSComponentState,
        ComponentCallback<PayByBankUSComponentState>,
        >,
    StoredPaymentComponentProvider<
        PayByBankUSComponent,
        PayByBankUSConfiguration,
        PayByBankUSComponentState,
        ComponentCallback<PayByBankUSComponentState>,
        >,
    SessionPaymentComponentProvider<
        PayByBankUSComponent,
        PayByBankUSConfiguration,
        PayByBankUSComponentState,
        SessionComponentCallback<PayByBankUSComponentState>,
        >,
    SessionStoredPaymentComponentProvider<
        PayByBankUSComponent,
        PayByBankUSConfiguration,
        PayByBankUSComponentState,
        SessionComponentCallback<PayByBankUSComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<PayByBankUSComponentState>,
        order: Order?,
        key: String?
    ): PayByBankUSComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams =
                ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                    checkoutConfiguration = checkoutConfiguration,
                    deviceLocale = localeProvider.getLocale(application),
                    dropInOverrideParams = dropInOverrideParams,
                    componentSessionParams = null,
                    componentConfiguration = checkoutConfiguration.getPayByBankUSConfiguration(),
                )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val payByBankUSDelegate = DefaultPayByBankUSDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            PayByBankUSComponent(
                payByBankUSDelegate = payByBankUSDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(
                    genericActionDelegate,
                    payByBankUSDelegate,
                ),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, PayByBankUSComponent::class.java]
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
        configuration: PayByBankUSConfiguration,
        application: Application,
        componentCallback: ComponentCallback<PayByBankUSComponentState>,
        order: Order?,
        key: String?
    ): PayByBankUSComponent {
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
        componentCallback: ComponentCallback<PayByBankUSComponentState>,
        order: Order?,
        key: String?
    ): PayByBankUSComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getPayByBankUSConfiguration(),
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val payByBankUSDelegate = StoredPayByBankUSDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                storedPaymentMethod = storedPaymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            PayByBankUSComponent(
                payByBankUSDelegate = payByBankUSDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, payByBankUSDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, PayByBankUSComponent::class.java]
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
        configuration: PayByBankUSConfiguration,
        application: Application,
        componentCallback: ComponentCallback<PayByBankUSComponentState>,
        order: Order?,
        key: String?
    ): PayByBankUSComponent {
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
        componentCallback: SessionComponentCallback<PayByBankUSComponentState>,
        key: String?
    ): PayByBankUSComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                componentConfiguration = checkoutConfiguration.getPayByBankUSConfiguration(),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val payByBankDelegate = DefaultPayByBankUSDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
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
            val sessionComponentEventHandler = SessionComponentEventHandler<PayByBankUSComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            PayByBankUSComponent(
                payByBankUSDelegate = payByBankDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, payByBankDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, PayByBankUSComponent::class.java]
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
        configuration: PayByBankUSConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<PayByBankUSComponentState>,
        key: String?
    ): PayByBankUSComponent {
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
        componentCallback: SessionComponentCallback<PayByBankUSComponentState>,
        key: String?
    ): PayByBankUSComponent {
        assertSupported(storedPaymentMethod)

        val genericStoredFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                componentConfiguration = checkoutConfiguration.getPayByBankUSConfiguration(),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val payByBankUSDelegate = StoredPayByBankUSDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                storedPaymentMethod = storedPaymentMethod,
                order = checkoutSession.order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
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

            val sessionComponentEventHandler =
                SessionComponentEventHandler<PayByBankUSComponentState>(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                )

            PayByBankUSComponent(
                payByBankUSDelegate = payByBankUSDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, payByBankUSDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericStoredFactory)[key, PayByBankUSComponent::class.java]
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
        configuration: PayByBankUSConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<PayByBankUSComponentState>,
        key: String?
    ): PayByBankUSComponent {
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
        return PayByBankUSComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return PayByBankUSComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
