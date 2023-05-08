/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsMapper
import com.adyen.checkout.components.core.internal.data.api.AnalyticsService
import com.adyen.checkout.components.core.internal.data.api.DefaultAnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyService
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.cse.internal.ClientSideEncrypter
import com.adyen.checkout.cse.internal.DateGenerator
import com.adyen.checkout.cse.internal.DefaultCardEncrypter
import com.adyen.checkout.cse.internal.DefaultGenericEncrypter
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.giftcard.internal.GiftCardComponentEventHandler
import com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentCallbackWrapper
import com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentEventHandler
import com.adyen.checkout.giftcard.internal.ui.DefaultGiftCardDelegate
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

class GiftCardComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) :
    PaymentComponentProvider<
        GiftCardComponent,
        GiftCardConfiguration,
        GiftCardComponentState,
        GiftCardComponentCallback>,
    SessionPaymentComponentProvider<
        GiftCardComponent,
        GiftCardConfiguration,
        GiftCardComponentState,
        SessionsGiftCardComponentCallback> {

    private val componentParamsMapper = ButtonComponentParamsMapper(overrideComponentParams, overrideSessionParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: GiftCardConfiguration,
        application: Application,
        componentCallback: GiftCardComponentCallback,
        order: Order?,
        key: String?,
    ): GiftCardComponent {
        assertSupported(paymentMethod)

        val clientSideEncrypter = ClientSideEncrypter()
        val dateGenerator = DateGenerator()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(configuration, null)
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val giftCardDelegate = DefaultGiftCardDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                analyticsRepository = analyticsRepository,
                publicKeyRepository = DefaultPublicKeyRepository(publicKeyService),
                componentParams = componentParams,
                cardEncrypter = cardEncrypter,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            GiftCardComponent(
                giftCardDelegate = giftCardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, giftCardDelegate),
                componentEventHandler = GiftCardComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, GiftCardComponent::class.java]
            .also { component ->
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
        configuration: GiftCardConfiguration,
        application: Application,
        componentCallback: SessionsGiftCardComponentCallback,
        key: String?
    ): GiftCardComponent {
        assertSupported(paymentMethod)

        val clientSideEncrypter = ClientSideEncrypter()
        val dateGenerator = DateGenerator()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(
                configuration = configuration,
                sessionParams = SessionParamsFactory.create(checkoutSession)
            )
            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)
            val analyticsService = AnalyticsService(httpClient)
            val analyticsRepository = DefaultAnalyticsRepository(
                packageName = application.packageName,
                locale = componentParams.shopperLocale,
                source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                analyticsService = analyticsService,
                analyticsMapper = AnalyticsMapper(),
            )

            val giftCardDelegate = DefaultGiftCardDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                analyticsRepository = analyticsRepository,
                publicKeyRepository = DefaultPublicKeyRepository(publicKeyService),
                componentParams = componentParams,
                cardEncrypter = cardEncrypter,
                submitHandler = SubmitHandler(savedStateHandle),
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

            val sessionsGiftCardComponentEventHandler = SessionsGiftCardComponentEventHandler(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            GiftCardComponent(
                giftCardDelegate = giftCardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, giftCardDelegate),
                componentEventHandler = sessionsGiftCardComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, GiftCardComponent::class.java]
            .also { component ->
                val internalComponentCallback = SessionsGiftCardComponentCallbackWrapper(
                    component,
                    componentCallback
                )
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, internalComponentCallback)
                }
            }
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return GiftCardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
