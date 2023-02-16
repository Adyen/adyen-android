/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.card.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.internal.data.api.BinLookupService
import com.adyen.checkout.card.internal.data.api.DefaultDetectCardTypeRepository
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate
import com.adyen.checkout.card.internal.ui.StoredCardDelegate
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AddressService
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.api.PublicKeyService
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultComponentEventHandler
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.DefaultAddressRepository
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
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
import com.adyen.checkout.sessions.provider.SessionStoredPaymentComponentProvider
import com.adyen.checkout.sessions.repository.SessionRepository

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardComponentProvider(
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) :
    PaymentComponentProvider<CardComponent, CardConfiguration, CardComponentState>,
    StoredPaymentComponentProvider<CardComponent, CardConfiguration, CardComponentState>,
    SessionPaymentComponentProvider<CardComponent, CardConfiguration, CardComponentState>,
    SessionStoredPaymentComponentProvider<CardComponent, CardConfiguration, CardComponentState> {

    private val componentParamsMapper = CardComponentParamsMapper()

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CardComponentState>,
        order: Order?,
        key: String?
    ): CardComponent {
        assertSupported(paymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParamsDefault(
                configuration,
                paymentMethod,
                overrideComponentParams,
                sessionSetupConfiguration
            )
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val genericEncrypter = DefaultGenericEncrypter()
            val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncrypter, binLookupService)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val cardValidationMapper = CardValidationMapper()
            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val cardDelegate = DefaultCardDelegate(
                observerRepository = PaymentObserverRepository(),
                publicKeyRepository = publicKeyRepository,
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = order,
                analyticsRepository = analyticsRepository,
                addressRepository = addressRepository,
                detectCardTypeRepository = detectCardTypeRepository,
                cardValidationMapper = cardValidationMapper,
                cardEncrypter = cardEncrypter,
                genericEncrypter = genericEncrypter,
                submitHandler = SubmitHandler(savedStateHandle)
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            CardComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java].also { component ->
            component.observe(lifecycleOwner) {
                component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
            }
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CardComponentState>,
        key: String?
    ): CardComponent {
        assertSupported(paymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParamsDefault(
                configuration,
                paymentMethod,
                overrideComponentParams,
                checkoutSession.sessionSetupResponse.configuration
            )
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val genericEncrypter = DefaultGenericEncrypter()
            val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncrypter, binLookupService)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val cardValidationMapper = CardValidationMapper()
            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val cardDelegate = DefaultCardDelegate(
                observerRepository = PaymentObserverRepository(),
                publicKeyRepository = publicKeyRepository,
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                analyticsRepository = analyticsRepository,
                addressRepository = addressRepository,
                detectCardTypeRepository = detectCardTypeRepository,
                cardValidationMapper = cardValidationMapper,
                cardEncrypter = cardEncrypter,
                genericEncrypter = genericEncrypter,
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
            val sessionComponentEventHandler = SessionComponentEventHandler<CardComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            CardComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java].also { component ->
            component.observe(lifecycleOwner) {
                component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
            }
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CardComponentState>,
        order: Order?,
        key: String?
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParamsStored(configuration, overrideComponentParams)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val genericEncrypter = DefaultGenericEncrypter()
            val cardEncrypter = DefaultCardEncrypter(genericEncrypter)

            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, storedPaymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val cardDelegate = StoredCardDelegate(
                observerRepository = PaymentObserverRepository(),
                storedPaymentMethod = storedPaymentMethod,
                order = order,
                componentParams = componentParams,
                analyticsRepository = analyticsRepository,
                cardEncrypter = cardEncrypter,
                publicKeyRepository = publicKeyRepository,
                submitHandler = SubmitHandler(savedStateHandle)
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            CardComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java].also { component ->
            component.observe(lifecycleOwner) {
                component.componentEventHandler.onPaymentComponentEvent(it, componentCallback)
            }
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CardComponentState>,
        key: String?
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParamsStored(configuration, overrideComponentParams)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val genericEncrypter = DefaultGenericEncrypter()
            val cardEncrypter = DefaultCardEncrypter(genericEncrypter)

            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, storedPaymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val cardDelegate = StoredCardDelegate(
                observerRepository = PaymentObserverRepository(),
                storedPaymentMethod = storedPaymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
                analyticsRepository = analyticsRepository,
                cardEncrypter = cardEncrypter,
                publicKeyRepository = publicKeyRepository,
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
            val sessionComponentEventHandler = SessionComponentEventHandler<CardComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            CardComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, factory)[key, CardComponent::class.java].also { component ->
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

    private fun assertSupported(storedPaymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(storedPaymentMethod)) {
            throw ComponentException("Unsupported payment method ${storedPaymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return CardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return CardComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
