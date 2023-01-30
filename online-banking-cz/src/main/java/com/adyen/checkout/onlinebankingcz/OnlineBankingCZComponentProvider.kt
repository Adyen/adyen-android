/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

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
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.PdfOpener
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback
import com.adyen.checkout.sessions.SessionHandler
import com.adyen.checkout.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.interactor.SessionInteractor
import com.adyen.checkout.sessions.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.repository.SessionRepository

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingCZComponentProvider(
    overrideComponentParams: ComponentParams? = null
) : SessionPaymentComponentProvider<
    OnlineBankingComponent<OnlineBankingCZPaymentMethod>,
    OnlineBankingCZConfiguration,
    PaymentComponentState<OnlineBankingCZPaymentMethod>
    > {

    private val componentParamsMapper = ButtonComponentParamsMapper(overrideComponentParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingCZConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: ComponentCallback<PaymentComponentState<OnlineBankingCZPaymentMethod>>,
        order: Order?,
        key: String?
    ): OnlineBankingComponent<OnlineBankingCZPaymentMethod> {
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

                val onlineBankingDelegate = DefaultOnlineBankingDelegate(
                    observerRepository = PaymentObserverRepository(),
                    pdfOpener = PdfOpener(),
                    paymentMethod = paymentMethod,
                    order = order,
                    componentParams = componentParams,
                    analyticsRepository = analyticsRepository,
                    termsAndConditionsUrl = OnlineBankingCZComponent.TERMS_CONDITIONS_URL,
                    submitHandler = SubmitHandler(savedStateHandle),
                ) { OnlineBankingCZPaymentMethod() }

                val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                    configuration = configuration.genericActionConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

                OnlineBankingCZComponent(
                    delegate = onlineBankingDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(
                        genericActionDelegate,
                        onlineBankingDelegate
                    ),
                    componentEventHandler = DefaultComponentEventHandler(componentCallback)
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingCZComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingCZConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: SessionComponentCallback<PaymentComponentState<OnlineBankingCZPaymentMethod>>,
        key: String?
    ): OnlineBankingComponent<OnlineBankingCZPaymentMethod> {
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

                val onlineBankingDelegate = DefaultOnlineBankingDelegate(
                    observerRepository = PaymentObserverRepository(),
                    pdfOpener = PdfOpener(),
                    paymentMethod = paymentMethod,
                    order = checkoutSession.order,
                    componentParams = componentParams,
                    analyticsRepository = analyticsRepository,
                    termsAndConditionsUrl = OnlineBankingCZComponent.TERMS_CONDITIONS_URL,
                    submitHandler = SubmitHandler(savedStateHandle),
                ) { OnlineBankingCZPaymentMethod() }

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

                OnlineBankingCZComponent(
                    delegate = onlineBankingDelegate,
                    genericActionDelegate = genericActionDelegate,
                    actionHandlingComponent = DefaultActionHandlingComponent(
                        genericActionDelegate,
                        onlineBankingDelegate
                    ),
                    componentEventHandler = sessionHandler
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingCZComponent::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.componentEventHandler::onPaymentComponentEvent)
            }
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return OnlineBankingCZComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
