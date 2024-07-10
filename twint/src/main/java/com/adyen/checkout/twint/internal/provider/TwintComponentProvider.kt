/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
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
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.twint.TwintComponent
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.TwintConfiguration
import com.adyen.checkout.twint.internal.ui.DefaultTwintDelegate
import com.adyen.checkout.twint.internal.ui.TwintDelegate
import com.adyen.checkout.twint.toCheckoutConfiguration
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

class TwintComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        TwintComponent,
        TwintConfiguration,
        TwintComponentState,
        ComponentCallback<TwintComponentState>,
        >,
    SessionPaymentComponentProvider<
        TwintComponent,
        TwintConfiguration,
        TwintComponentState,
        SessionComponentCallback<TwintComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<TwintComponentState>,
        order: Order?,
        key: String?
    ): TwintComponent {
        assertSupported(paymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CommonComponentParamsMapper().mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams.commonComponentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val twintDelegate = DefaultTwintDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                analyticsManager = analyticsManager,
                paymentMethod = paymentMethod,
                order = order,
                componentParams = componentParams.commonComponentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = twintDelegate,
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, TwintComponent::class.java]
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
        configuration: TwintConfiguration,
        application: Application,
        componentCallback: ComponentCallback<TwintComponentState>,
        order: Order?,
        key: String?
    ): TwintComponent {
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
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<TwintComponentState>,
        key: String?
    ): TwintComponent {
        assertSupported(paymentMethod)

        val viewModelFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CommonComponentParamsMapper().mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.commonComponentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams.commonComponentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val cashAppPayDelegate = DefaultTwintDelegate(
                submitHandler = SubmitHandler(savedStateHandle),
                analyticsManager = analyticsManager,
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams.commonComponentParams,
            )

            val sessionComponentEventHandler = createSessionComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
                httpClient = httpClient,
                componentParams = componentParams.commonComponentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = cashAppPayDelegate,
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, viewModelFactory)[key, TwintComponent::class.java]
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
        configuration: TwintConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<TwintComponentState>,
        key: String?
    ): TwintComponent {
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

    private fun createComponent(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
        delegate: TwintDelegate,
        componentEventHandler: ComponentEventHandler<TwintComponentState>,
    ): TwintComponent {
        val genericActionDelegate = GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            application = application,
        )

        return TwintComponent(
            twintDelegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, delegate),
            componentEventHandler = componentEventHandler,
        )
    }

    private fun createSessionComponentEventHandler(
        savedStateHandle: SavedStateHandle,
        checkoutSession: CheckoutSession,
        httpClient: HttpClient,
        componentParams: ComponentParams,
    ): SessionComponentEventHandler<TwintComponentState> {
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

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return TwintComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
