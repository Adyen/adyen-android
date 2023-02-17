/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.internal.ui.DefaultBcmcDelegate
import com.adyen.checkout.bcmc.internal.ui.model.BcmcComponentParamsMapper
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.api.PublicKeyService
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultComponentEventHandler
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.ClientSideEncrypter
import com.adyen.checkout.cse.DateGenerator
import com.adyen.checkout.cse.DefaultCardEncrypter
import com.adyen.checkout.cse.DefaultGenericEncrypter
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
class BcmcComponentProvider(
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) :
    PaymentComponentProvider<BcmcComponent, BcmcConfiguration, PaymentComponentState<CardPaymentMethod>>,
    SessionPaymentComponentProvider<BcmcComponent, BcmcConfiguration, PaymentComponentState<CardPaymentMethod>> {

    private val componentParamsMapper = BcmcComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration,
        application: Application,
        componentCallback: ComponentCallback<PaymentComponentState<CardPaymentMethod>>,
        order: Order?,
        key: String?,
    ): BcmcComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(
            configuration,
            overrideComponentParams,
            sessionSetupConfiguration
        )
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val publicKeyService = PublicKeyService(httpClient)
        val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
        val cardValidationMapper = CardValidationMapper()
        val dateGenerator = DateGenerator()
        val clientSideEncrypter = ClientSideEncrypter()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(clientSideEncrypter, genericEncrypter, dateGenerator)
        val analyticsService = AnalyticsService(httpClient)
        val analyticsRepository = DefaultAnalyticsRepository(
            packageName = application.packageName,
            locale = componentParams.shopperLocale,
            source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
            analyticsService = analyticsService,
            analyticsMapper = AnalyticsMapper(),
        )

        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val bcmcDelegate = DefaultBcmcDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                publicKeyRepository = publicKeyRepository,
                componentParams = componentParams,
                cardValidationMapper = cardValidationMapper,
                cardEncrypter = cardEncrypter,
                analyticsRepository = analyticsRepository,
                submitHandler = SubmitHandler(savedStateHandle)
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            BcmcComponent(
                bcmcDelegate = bcmcDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, bcmcDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory)[key, BcmcComponent::class.java].also { component ->
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
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: BcmcConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<PaymentComponentState<CardPaymentMethod>>,
        key: String?
    ): BcmcComponent {
        assertSupported(paymentMethod)

        val componentParams = componentParamsMapper.mapToParams(
            configuration,
            overrideComponentParams,
            checkoutSession.sessionSetupResponse.configuration
        )
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val publicKeyService = PublicKeyService(httpClient)
        val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
        val cardValidationMapper = CardValidationMapper()
        val dateGenerator = DateGenerator()
        val clientSideEncrypter = ClientSideEncrypter()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(clientSideEncrypter, genericEncrypter, dateGenerator)
        val analyticsService = AnalyticsService(httpClient)
        val analyticsRepository = DefaultAnalyticsRepository(
            packageName = application.packageName,
            locale = componentParams.shopperLocale,
            source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
            analyticsService = analyticsService,
            analyticsMapper = AnalyticsMapper(),
        )

        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val bcmcDelegate = DefaultBcmcDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                publicKeyRepository = publicKeyRepository,
                componentParams = componentParams,
                cardValidationMapper = cardValidationMapper,
                cardEncrypter = cardEncrypter,
                analyticsRepository = analyticsRepository,
                submitHandler = SubmitHandler(savedStateHandle)
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

            val sessionComponentEventHandler = SessionComponentEventHandler<PaymentComponentState<CardPaymentMethod>>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            BcmcComponent(
                bcmcDelegate = bcmcDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, bcmcDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, bcmcFactory)[key, BcmcComponent::class.java].also { component ->
            component.observe(lifecycleOwner) {
                component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
            }
        }
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BcmcComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
