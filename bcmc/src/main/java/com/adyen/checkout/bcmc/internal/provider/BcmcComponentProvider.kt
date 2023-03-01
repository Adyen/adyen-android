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
import com.adyen.checkout.bcmc.BcmcComponentState
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.internal.ui.DefaultBcmcDelegate
import com.adyen.checkout.bcmc.internal.ui.model.BcmcComponentParamsMapper
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ComponentCallback
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyService
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.cse.internal.ClientSideEncrypter
import com.adyen.checkout.cse.internal.DateGenerator
import com.adyen.checkout.cse.internal.DefaultCardEncrypter
import com.adyen.checkout.cse.internal.DefaultGenericEncrypter
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionSetupConfiguration
import com.adyen.checkout.sessions.core.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BcmcComponentProvider(
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) :
    PaymentComponentProvider<BcmcComponent, BcmcConfiguration, BcmcComponentState>,
    SessionPaymentComponentProvider<BcmcComponent, BcmcConfiguration, BcmcComponentState> {

    private val componentParamsMapper = BcmcComponentParamsMapper()

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
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
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
        componentCallback: SessionComponentCallback<BcmcComponentState>,
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
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
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

            val sessionComponentEventHandler = SessionComponentEventHandler<BcmcComponentState>(
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
