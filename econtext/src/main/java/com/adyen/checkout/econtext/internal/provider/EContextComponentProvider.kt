/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/1/2023.
 */

package com.adyen.checkout.econtext.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
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
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepositoryData
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.components.core.paymentmethod.EContextPaymentMethod
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.econtext.internal.EContextComponent
import com.adyen.checkout.econtext.internal.EContextConfiguration
import com.adyen.checkout.econtext.internal.ui.DefaultEContextDelegate
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
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

@Suppress("TooManyFunctions", "ktlint:standard:type-parameter-list-spacing")
abstract class EContextComponentProvider<
    ComponentT : EContextComponent<PaymentMethodT, ComponentStateT>,
    ConfigurationT : EContextConfiguration,
    PaymentMethodT : EContextPaymentMethod,
    ComponentStateT : PaymentComponentState<PaymentMethodT>,
    >
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val componentClass: Class<ComponentT>,
    private val dropInOverrideParams: DropInOverrideParams?,
    private val analyticsRepository: AnalyticsRepository?,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) : PaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallback<ComponentStateT>>,
    SessionPaymentComponentProvider<
        ComponentT,
        ConfigurationT,
        ComponentStateT,
        SessionComponentCallback<ComponentStateT>,
        > {

    override fun get(
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

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
                val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                    checkoutConfiguration = checkoutConfiguration,
                    deviceLocale = localeProvider.getLocale(application),
                    dropInOverrideParams = dropInOverrideParams,
                    componentSessionParams = null,
                    componentConfiguration = getConfiguration(checkoutConfiguration),
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
                val eContextDelegate = DefaultEContextDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                    typedPaymentMethodFactory = { createPaymentMethod() },
                    componentStateFactory = ::createComponentState,
                )

                val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

                createComponent(
                    delegate = eContextDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, eContextDelegate),
                    componentEventHandler = DefaultComponentEventHandler(),
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, componentClass].also { component ->
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
    override fun get(
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

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
                val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                    checkoutConfiguration = checkoutConfiguration,
                    deviceLocale = localeProvider.getLocale(application),
                    dropInOverrideParams = dropInOverrideParams,
                    componentSessionParams = SessionParamsFactory.create(checkoutSession),
                    componentConfiguration = getConfiguration(checkoutConfiguration),
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
                val eContextDelegate = DefaultEContextDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = checkoutSession.order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                    typedPaymentMethodFactory = { createPaymentMethod() },
                    componentStateFactory = ::createComponentState,
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
                val sessionComponentEventHandler = SessionComponentEventHandler<ComponentStateT>(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                )

                createComponent(
                    delegate = eContextDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, eContextDelegate),
                    componentEventHandler = sessionComponentEventHandler,
                )
            }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, componentClass].also { component ->
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

    protected abstract fun createComponentState(
        data: PaymentComponentData<PaymentMethodT>,
        isInputValid: Boolean,
        isReady: Boolean
    ): ComponentStateT

    abstract fun createComponent(
        delegate: EContextDelegate<PaymentMethodT, ComponentStateT>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<ComponentStateT>,
    ): ComponentT

    abstract fun createPaymentMethod(): PaymentMethodT

    abstract fun getSupportedPaymentMethods(): List<String>

    protected abstract fun getConfiguration(checkoutConfiguration: CheckoutConfiguration): ConfigurationT?

    protected abstract fun getCheckoutConfiguration(configuration: ConfigurationT): CheckoutConfiguration

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return getSupportedPaymentMethods().contains(paymentMethod.type)
    }
}
