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
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionComponentProvider
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultComponentEventHandler
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.EContextPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.econtext.internal.ui.DefaultEContextDelegate
import com.adyen.checkout.econtext.EContextComponent
import com.adyen.checkout.econtext.EContextConfiguration
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback
import com.adyen.checkout.sessions.SessionComponentEventHandler
import com.adyen.checkout.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.interactor.SessionInteractor
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration
import com.adyen.checkout.sessions.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.repository.SessionRepository

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class EContextComponentProvider<
    ComponentT : EContextComponent<PaymentMethodT>,
    ConfigurationT : EContextConfiguration,
    PaymentMethodT : EContextPaymentMethod>(
    private val componentClass: Class<ComponentT>,
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : PaymentComponentProvider<ComponentT, ConfigurationT, PaymentComponentState<PaymentMethodT>>,
    SessionPaymentComponentProvider<ComponentT, ConfigurationT, PaymentComponentState<PaymentMethodT>> {

    private val componentParamsMapper = ButtonComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallback<PaymentComponentState<PaymentMethodT>>,
        order: Order?,
        key: String?
    ): ComponentT {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )
                val eContextDelegate = DefaultEContextDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                    typedPaymentMethodFactory = { createPaymentMethod() }
                )

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

                createComponent(
                    delegate = eContextDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, eContextDelegate),
                    componentEventHandler = DefaultComponentEventHandler()
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
        componentCallback: SessionComponentCallback<PaymentComponentState<PaymentMethodT>>,
        key: String?
    ): ComponentT {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )
                val eContextDelegate = DefaultEContextDelegate(
                    observerRepository = PaymentObserverRepository(),
                    componentParams = componentParams,
                    paymentMethod = paymentMethod,
                    order = checkoutSession.order,
                    analyticsRepository = analyticsRepository,
                    submitHandler = SubmitHandler(savedStateHandle),
                    typedPaymentMethodFactory = { createPaymentMethod() }
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
                val sessionComponentEventHandler = SessionComponentEventHandler<PaymentComponentState<PaymentMethodT>>(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer
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

    abstract fun createComponent(
        delegate: EContextDelegate<PaymentMethodT>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<PaymentMethodT>>,
    ): ComponentT

    abstract fun createPaymentMethod(): PaymentMethodT

    abstract fun getSupportedPaymentMethods(): List<String>

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return getSupportedPaymentMethods().contains(paymentMethod.type)
    }
}
