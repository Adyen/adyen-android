/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.ach.ACHDirectDebitComponent
import com.adyen.checkout.ach.ACHDirectDebitComponentState
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.ach.internal.ui.ACHDirectDebitDelegate
import com.adyen.checkout.ach.internal.ui.DefaultACHDirectDebitDelegate
import com.adyen.checkout.ach.internal.ui.StoredACHDirectDebitDelegate
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParams
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParamsMapper
import com.adyen.checkout.ach.toCheckoutConfiguration
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyService
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.provider.StoredPaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.data.api.HttpClient
import com.adyen.checkout.core.old.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.old.internal.util.LocaleProvider
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
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.old.internal.data.api.AddressService
import com.adyen.checkout.ui.core.old.internal.data.api.DefaultAddressRepository

@Suppress("TooManyFunctions")
class ACHDirectDebitComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        ACHDirectDebitComponentState,
        ComponentCallback<ACHDirectDebitComponentState>,
        >,
    StoredPaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        ACHDirectDebitComponentState,
        ComponentCallback<ACHDirectDebitComponentState>,
        >,
    SessionPaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        ACHDirectDebitComponentState,
        SessionComponentCallback<ACHDirectDebitComponentState>,
        >,
    SessionStoredPaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        ACHDirectDebitComponentState,
        SessionComponentCallback<ACHDirectDebitComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<ACHDirectDebitComponentState>,
        order: Order?,
        key: String?,
    ): ACHDirectDebitComponent {
        assertSupported(paymentMethod)

        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val achDelegate = createDefaultDelegate(
                paymentMethod = paymentMethod,
                savedStateHandle = savedStateHandle,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                httpClient = httpClient,
                order = order,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = achDelegate,
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }
        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory,
        )[key, ACHDirectDebitComponent::class.java].also { component ->
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
        configuration: ACHDirectDebitConfiguration,
        application: Application,
        componentCallback: ComponentCallback<ACHDirectDebitComponentState>,
        order: Order?,
        key: String?,
    ): ACHDirectDebitComponent {
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

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<ACHDirectDebitComponentState>,
        key: String?
    ): ACHDirectDebitComponent {
        assertSupported(paymentMethod)

        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val achDelegate = createDefaultDelegate(
                paymentMethod = paymentMethod,
                savedStateHandle = savedStateHandle,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                httpClient = httpClient,
                order = checkoutSession.order,
            )

            val sessionComponentEventHandler = createSessionComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
                httpClient = httpClient,
                componentParams = componentParams,
            )

            createComponent(
                checkoutConfiguration,
                savedStateHandle,
                application,
                achDelegate,
                sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory,
        )[key, ACHDirectDebitComponent::class.java].also { component ->
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
        configuration: ACHDirectDebitConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<ACHDirectDebitComponentState>,
        key: String?
    ): ACHDirectDebitComponent {
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

    @Suppress("LongParameterList")
    private fun createDefaultDelegate(
        paymentMethod: PaymentMethod,
        savedStateHandle: SavedStateHandle,
        componentParams: ACHDirectDebitComponentParams,
        analyticsManager: AnalyticsManager,
        httpClient: HttpClient,
        order: Order?,
    ): DefaultACHDirectDebitDelegate {
        val publicKeyService = PublicKeyService(httpClient)
        val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
        val addressService = AddressService(httpClient)
        val addressRepository = DefaultAddressRepository(addressService)
        val genericEncryptor = GenericEncryptorFactory.provide()
        return DefaultACHDirectDebitDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = paymentMethod,
            analyticsManager = analyticsManager,
            publicKeyRepository = publicKeyRepository,
            addressRepository = addressRepository,
            submitHandler = SubmitHandler(savedStateHandle),
            genericEncryptor = genericEncryptor,
            componentParams = componentParams,
            order = order,
        )
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<ACHDirectDebitComponentState>,
        order: Order?,
        key: String?
    ): ACHDirectDebitComponent {
        assertSupported(storedPaymentMethod)

        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val achDelegate = createStoredDelegate(
                paymentMethod = storedPaymentMethod,
                componentParams = componentParams,
                analyticsManager = analyticsManager,
                order = order,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = achDelegate,
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory,
        )[key, ACHDirectDebitComponent::class.java].also { component ->
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
        configuration: ACHDirectDebitConfiguration,
        application: Application,
        componentCallback: ComponentCallback<ACHDirectDebitComponentState>,
        order: Order?,
        key: String?
    ): ACHDirectDebitComponent {
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
        storedPaymentMethod: StoredPaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<ACHDirectDebitComponentState>,
        key: String?
    ): ACHDirectDebitComponent {
        assertSupported(storedPaymentMethod)

        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val achDelegate = createStoredDelegate(
                paymentMethod = storedPaymentMethod,
                analyticsManager = analyticsManager,
                componentParams = componentParams,
                order = checkoutSession.order,
            )

            val sessionComponentEventHandler = createSessionComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutSession,
                httpClient = httpClient,
                componentParams = componentParams,
            )

            createComponent(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
                delegate = achDelegate,
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory,
        )[key, ACHDirectDebitComponent::class.java].also { component ->
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
        configuration: ACHDirectDebitConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<ACHDirectDebitComponentState>,
        key: String?
    ): ACHDirectDebitComponent {
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

    private fun createStoredDelegate(
        paymentMethod: StoredPaymentMethod,
        componentParams: ACHDirectDebitComponentParams,
        analyticsManager: AnalyticsManager,
        order: Order?
    ): StoredACHDirectDebitDelegate {
        return StoredACHDirectDebitDelegate(
            observerRepository = PaymentObserverRepository(),
            storedPaymentMethod = paymentMethod,
            analyticsManager = analyticsManager,
            componentParams = componentParams,
            order = order,
        )
    }

    private fun createComponent(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
        delegate: ACHDirectDebitDelegate,
        componentEventHandler: ComponentEventHandler<ACHDirectDebitComponentState>,
    ): ACHDirectDebitComponent {
        val genericActionDelegate = GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            application = application,
        )

        return ACHDirectDebitComponent(
            achDirectDebitDelegate = delegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, delegate),
            componentEventHandler = componentEventHandler,
        )
    }

    private fun createSessionComponentEventHandler(
        savedStateHandle: SavedStateHandle,
        checkoutSession: CheckoutSession,
        httpClient: HttpClient,
        componentParams: ACHDirectDebitComponentParams,
    ): SessionComponentEventHandler<ACHDirectDebitComponentState> {
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

    private fun assertSupported(storedPaymentMethod: StoredPaymentMethod) {
        if (!isPaymentMethodSupported(storedPaymentMethod)) {
            throw ComponentException("Unsupported payment method ${storedPaymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return ACHDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }

    override fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return ACHDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(storedPaymentMethod.type)
    }
}
