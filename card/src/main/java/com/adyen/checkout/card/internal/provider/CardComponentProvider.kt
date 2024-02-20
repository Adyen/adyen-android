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
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.internal.data.api.BinLookupService
import com.adyen.checkout.card.internal.data.api.DefaultDetectCardTypeRepository
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate
import com.adyen.checkout.card.internal.ui.StoredCardDelegate
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.model.InstallmentsParamsMapper
import com.adyen.checkout.card.toCheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepositoryData
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyService
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.provider.StoredPaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.cse.internal.CardEncryptorFactory
import com.adyen.checkout.cse.internal.GenericEncryptorFactory
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
import com.adyen.checkout.ui.core.internal.data.api.AddressService
import com.adyen.checkout.ui.core.internal.data.api.DefaultAddressRepository
import com.adyen.checkout.ui.core.internal.ui.DefaultAddressLookupDelegate
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

@Suppress("TooManyFunctions")
class CardComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        CardComponent,
        CardConfiguration,
        CardComponentState,
        ComponentCallback<CardComponentState>,
        >,
    StoredPaymentComponentProvider<
        CardComponent,
        CardConfiguration,
        CardComponentState,
        ComponentCallback<CardComponentState>,
        >,
    SessionPaymentComponentProvider<
        CardComponent,
        CardConfiguration,
        CardComponentState,
        SessionComponentCallback<CardComponentState>,
        >,
    SessionStoredPaymentComponentProvider<
        CardComponent,
        CardConfiguration,
        CardComponentState,
        SessionComponentCallback<CardComponentState>,
        > {

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CardComponentState>,
        order: Order?,
        key: String?
    ): CardComponent {
        assertSupported(paymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CardComponentParamsMapper(
                CommonComponentParamsMapper(),
                InstallmentsParamsMapper(),
            ).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                paymentMethod = paymentMethod,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val genericEncryptor = GenericEncryptorFactory.provide()
            val cardEncryptor = CardEncryptorFactory.provide()
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val cardValidationMapper = CardValidationMapper()

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
                cardEncryptor = cardEncryptor,
                genericEncryptor = genericEncryptor,
                submitHandler = SubmitHandler(savedStateHandle),
                addressLookupDelegate = DefaultAddressLookupDelegate(
                    addressRepository = DefaultAddressRepository(AddressService(httpClient)),
                    shopperLocale = componentParams.shopperLocale,
                ),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
                checkoutConfiguration = checkoutConfiguration,
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
        paymentMethod: PaymentMethod,
        configuration: CardConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CardComponentState>,
        order: Order?,
        key: String?
    ): CardComponent {
        return get(
            savedStateRegistryOwner,
            viewModelStoreOwner,
            lifecycleOwner,
            paymentMethod,
            configuration.toCheckoutConfiguration(),
            application,
            componentCallback,
            order,
            key,
        )
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CardComponentState>,
        key: String?
    ): CardComponent {
        assertSupported(paymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CardComponentParamsMapper(
                CommonComponentParamsMapper(),
                InstallmentsParamsMapper(),
            ).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                paymentMethod = paymentMethod,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val genericEncryptor = GenericEncryptorFactory.provide()
            val cardEncryptor = CardEncryptorFactory.provide()
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val cardValidationMapper = CardValidationMapper()

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
                cardEncryptor = cardEncryptor,
                genericEncryptor = genericEncryptor,
                submitHandler = SubmitHandler(savedStateHandle),
                addressLookupDelegate = DefaultAddressLookupDelegate(
                    addressRepository = DefaultAddressRepository(AddressService(httpClient)),
                    shopperLocale = componentParams.shopperLocale,
                ),
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

    @Suppress("LongParameterList")
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
        return get(
            savedStateRegistryOwner,
            viewModelStoreOwner,
            lifecycleOwner,
            checkoutSession,
            paymentMethod,
            configuration.toCheckoutConfiguration(),
            application,
            componentCallback,
            key,
        )
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<CardComponentState>,
        order: Order?,
        key: String?
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CardComponentParamsMapper(
                CommonComponentParamsMapper(),
                InstallmentsParamsMapper(),
            ).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                storedPaymentMethod = storedPaymentMethod,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val cardEncryptor = CardEncryptorFactory.provide()

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

            val cardDelegate = StoredCardDelegate(
                observerRepository = PaymentObserverRepository(),
                storedPaymentMethod = storedPaymentMethod,
                order = order,
                componentParams = componentParams,
                analyticsRepository = analyticsRepository,
                cardEncryptor = cardEncryptor,
                publicKeyRepository = publicKeyRepository,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(dropInOverrideParams).getDelegate(
                checkoutConfiguration = checkoutConfiguration,
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

    @Suppress("LongParameterList")
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
        return get(
            savedStateRegistryOwner,
            viewModelStoreOwner,
            lifecycleOwner,
            storedPaymentMethod,
            configuration.toCheckoutConfiguration(),
            application,
            componentCallback,
            order,
            key,
        )
    }

    @Suppress("LongParameterList", "LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<CardComponentState>,
        key: String?
    ): CardComponent {
        assertSupported(storedPaymentMethod)

        val factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = CardComponentParamsMapper(
                CommonComponentParamsMapper(),
                InstallmentsParamsMapper(),
            ).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                storedPaymentMethod = storedPaymentMethod,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val cardEncryptor = CardEncryptorFactory.provide()

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

            val cardDelegate = StoredCardDelegate(
                observerRepository = PaymentObserverRepository(),
                storedPaymentMethod = storedPaymentMethod,
                order = checkoutSession.order,
                componentParams = componentParams,
                analyticsRepository = analyticsRepository,
                cardEncryptor = cardEncryptor,
                publicKeyRepository = publicKeyRepository,
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
        return CardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return CardComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
