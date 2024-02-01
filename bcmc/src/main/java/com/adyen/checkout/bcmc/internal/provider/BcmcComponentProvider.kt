/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/8/2023.
 */

package com.adyen.checkout.bcmc.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.bcmc.BcmcComponent
import com.adyen.checkout.bcmc.BcmcComponentState
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.internal.ui.model.BcmcComponentParamsMapper
import com.adyen.checkout.bcmc.toCheckoutConfiguration
import com.adyen.checkout.card.internal.data.api.BinLookupService
import com.adyen.checkout.card.internal.data.api.DefaultDetectCardTypeRepository
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
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
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
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
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.data.api.AddressService
import com.adyen.checkout.ui.core.internal.data.api.DefaultAddressRepository
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

class BcmcComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val isCreatedByDropIn: Boolean = false,
    overrideSessionParams: SessionParams? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
) :
    PaymentComponentProvider<
        BcmcComponent,
        BcmcConfiguration,
        BcmcComponentState,
        ComponentCallback<BcmcComponentState>,
        >,
    SessionPaymentComponentProvider<
        BcmcComponent,
        BcmcConfiguration,
        BcmcComponentState,
        SessionComponentCallback<BcmcComponentState>,
        > {

    private val componentParamsMapper = BcmcComponentParamsMapper(isCreatedByDropIn, overrideSessionParams)

    @Suppress("LongMethod")
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BcmcComponentState>,
        order: Order?,
        key: String?,
    ): BcmcComponent {
        assertSupported(paymentMethod)

        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(checkoutConfiguration, null, paymentMethod)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val cardValidationMapper = CardValidationMapper()
            val cardEncryptor = CardEncryptorFactory.provide()
            val genericEncryptor = GenericEncryptorFactory.provide()
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)

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
            )

            val genericActionDelegate = GenericActionComponentProvider(isCreatedByDropIn).getDelegate(
                checkoutConfiguration = checkoutConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            BcmcComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, bcmcFactory)[key, BcmcComponent::class.java].also { component ->
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
        configuration: BcmcConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BcmcComponentState>,
        order: Order?,
        key: String?,
    ): BcmcComponent {
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
        componentCallback: SessionComponentCallback<BcmcComponentState>,
        key: String?
    ): BcmcComponent {
        assertSupported(paymentMethod)
        val bcmcFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                sessionParams = SessionParamsFactory.create(checkoutSession),
                paymentMethod = paymentMethod,
            )
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val cardValidationMapper = CardValidationMapper()
            val cardEncryptor = CardEncryptorFactory.provide()
            val genericEncryptor = GenericEncryptorFactory.provide()
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val binLookupService = BinLookupService(httpClient)
            val detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)

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
            )

            val genericActionDelegate = GenericActionComponentProvider(isCreatedByDropIn).getDelegate(
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

            val sessionComponentEventHandler = SessionComponentEventHandler<BcmcComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            BcmcComponent(
                cardDelegate = cardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, cardDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(
            viewModelStoreOwner,
            bcmcFactory,
        )[key, BcmcComponent::class.java].also { component ->
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
        configuration: BcmcConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<BcmcComponentState>,
        key: String?
    ): BcmcComponent {
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

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return BcmcComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
