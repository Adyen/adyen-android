/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/1/2023.
 */

package com.adyen.checkout.issuerlist.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.components.core.paymentmethod.IssuerListPaymentMethod
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.old.internal.util.LocaleProvider
import com.adyen.checkout.issuerlist.internal.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import com.adyen.checkout.issuerlist.internal.ui.DefaultIssuerListDelegate
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListComponentParams
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListComponentParamsMapper
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler

@Suppress("TooManyFunctions", "ktlint:standard:type-parameter-list-spacing")
abstract class IssuerListComponentProvider<
    ComponentT : IssuerListComponent<PaymentMethodT, ComponentStateT>,
    ConfigurationT : IssuerListConfiguration,
    PaymentMethodT : IssuerListPaymentMethod,
    ComponentStateT : PaymentComponentState<PaymentMethodT>
    >
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val componentClass: Class<ComponentT>,
    private val dropInOverrideParams: DropInOverrideParams?,
    private val analyticsManager: AnalyticsManager? = null,
    private val hideIssuerLogosDefaultValue: Boolean = false,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallback<ComponentStateT>>,
    SessionPaymentComponentProvider<
        ComponentT,
        ConfigurationT,
        ComponentStateT,
        SessionComponentCallback<ComponentStateT>,
        > {

    final override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?
    ): ComponentT {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = IssuerListComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                hideIssuerLogosDefaultValue = hideIssuerLogosDefaultValue,
                componentConfiguration = getConfiguration(checkoutConfiguration),
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val issuerListDelegate = createDefaultDelegate(
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = order,
                savedStateHandle = savedStateHandle,
                analyticsManager = analyticsManager,
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            createComponent(
                issuerListDelegate,
                genericActionDelegate,
                DefaultActionHandlingComponent(genericActionDelegate, issuerListDelegate),
                DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, componentClass]
            .also { component ->
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
                }
            }
    }

    final override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?
    ): ComponentT {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            paymentMethod = paymentMethod,
            checkoutConfiguration = getCheckoutConfiguration(configuration),
            application = application,
            componentCallback = componentCallback,
            order = order,
            key = key,
        )
    }

    @Suppress("LongMethod")
    final override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String?
    ): ComponentT {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = IssuerListComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                hideIssuerLogosDefaultValue = hideIssuerLogosDefaultValue,
                componentConfiguration = getConfiguration(checkoutConfiguration),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )
            val issuerListDelegate = createDefaultDelegate(
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                savedStateHandle = savedStateHandle,
                analyticsManager = analyticsManager,
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
                analyticsManager = analyticsManager,
            )
            val sessionComponentEventHandler = SessionComponentEventHandler<ComponentStateT>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            createComponent(
                issuerListDelegate,
                genericActionDelegate,
                DefaultActionHandlingComponent(genericActionDelegate, issuerListDelegate),
                sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, componentClass]
            .also { component ->
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
                }
            }
    }

    @Suppress("LongMethod")
    final override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String?
    ): ComponentT {
        return get(
            savedStateRegistryOwner = savedStateRegistryOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycleOwner = lifecycleOwner,
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            checkoutConfiguration = getCheckoutConfiguration(configuration),
            application = application,
            componentCallback = componentCallback,
            key = key,
        )
    }

    private fun createDefaultDelegate(
        componentParams: IssuerListComponentParams,
        paymentMethod: PaymentMethod,
        order: Order?,
        savedStateHandle: SavedStateHandle,
        analyticsManager: AnalyticsManager,
    ): DefaultIssuerListDelegate<PaymentMethodT, ComponentStateT> {
        return DefaultIssuerListDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = componentParams,
            paymentMethod = paymentMethod,
            order = order,
            analyticsManager = analyticsManager,
            submitHandler = SubmitHandler(savedStateHandle),
            typedPaymentMethodFactory = ::createPaymentMethod,
            componentStateFactory = ::createComponentState,
        )
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    protected abstract fun createComponentState(
        data: PaymentComponentData<PaymentMethodT>,
        isInputValid: Boolean,
        isReady: Boolean
    ): ComponentStateT

    protected abstract fun createComponent(
        delegate: IssuerListDelegate<PaymentMethodT, ComponentStateT>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<ComponentStateT>,
    ): ComponentT

    protected abstract fun createPaymentMethod(): PaymentMethodT

    protected abstract fun getSupportedPaymentMethods(): List<String>

    protected abstract fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): ConfigurationT?

    protected abstract fun getCheckoutConfiguration(configuration: ConfigurationT): CheckoutConfiguration

    final override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return getSupportedPaymentMethods().contains(paymentMethod.type)
    }
}
