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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.ach.ACHDirectDebitComponent
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParamsMapper
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.ach.internal.ui.DefaultACHDirectDebitDelegate
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
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
import com.adyen.checkout.components.model.payments.request.ACHDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.DefaultAddressRepository
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
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
class ACHDirectDebitComponentProvider(
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) :
    PaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        PaymentComponentState<ACHDirectDebitPaymentMethod>
        >,
    SessionPaymentComponentProvider<
        ACHDirectDebitComponent,
        ACHDirectDebitConfiguration,
        PaymentComponentState<ACHDirectDebitPaymentMethod>
        > {

    private val componentParamsMapper = ACHDirectDebitComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: ACHDirectDebitConfiguration,
        application: Application,
        componentCallback: ComponentCallback<PaymentComponentState<ACHDirectDebitPaymentMethod>>,
        order: Order?,
        key: String?
    ): ACHDirectDebitComponent {
        assertSupported(paymentMethod)
        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val analyticsService = AnalyticsService(httpClient)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val genericEncrypter = DefaultGenericEncrypter()
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val achDelegate = DefaultACHDirectDebitDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                analyticsRepository = analyticsRepository,
                publicKeyRepository = publicKeyRepository,
                addressRepository = addressRepository,
                submitHandler = SubmitHandler(savedStateHandle),
                genericEncrypter = genericEncrypter,
                componentParams = componentParams,
                order = order
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            ACHDirectDebitComponent(
                achDirectDebitDelegate = achDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, achDelegate),
                componentEventHandler = DefaultComponentEventHandler()
            )
        }
        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory
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
        componentCallback: SessionComponentCallback<PaymentComponentState<ACHDirectDebitPaymentMethod>>,
        key: String?
    ): ACHDirectDebitComponent {
        assertSupported(paymentMethod)
        val achFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
            val analyticsService = AnalyticsService(httpClient)
            val addressService = AddressService(httpClient)
            val addressRepository = DefaultAddressRepository(addressService)
            val genericEncrypter = DefaultGenericEncrypter()
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val achDelegate = DefaultACHDirectDebitDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                analyticsRepository = analyticsRepository,
                publicKeyRepository = publicKeyRepository,
                addressRepository = addressRepository,
                submitHandler = SubmitHandler(savedStateHandle),
                genericEncrypter = genericEncrypter,
                componentParams = componentParams,
                order = checkoutSession.order
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

            val sessionComponentEventHandler =
                SessionComponentEventHandler<PaymentComponentState<ACHDirectDebitPaymentMethod>>(
                    sessionInteractor = sessionInteractor,
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                )

            ACHDirectDebitComponent(
                achDirectDebitDelegate = achDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, achDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }
        return ViewModelProvider(
            viewModelStoreOwner,
            achFactory
        )[key, ACHDirectDebitComponent::class.java].also { component ->
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
        return ACHDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
