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
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.api.PublicKeyService
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.DefaultComponentEventHandler
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.internal.ClientSideEncrypter
import com.adyen.checkout.cse.internal.DateGenerator
import com.adyen.checkout.cse.internal.DefaultCardEncrypter
import com.adyen.checkout.cse.internal.DefaultGenericEncrypter
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.internal.ui.DefaultGiftCardDelegate
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback
import com.adyen.checkout.sessions.SessionSetupConfiguration
import com.adyen.checkout.sessions.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.internal.SessionInteractor
import com.adyen.checkout.sessions.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.internal.data.api.SessionService
import com.adyen.checkout.sessions.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GiftCardComponentProvider(
    private val overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) :
    PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration, GiftCardComponentState>,
    SessionPaymentComponentProvider<GiftCardComponent, GiftCardConfiguration, GiftCardComponentState> {

    private val componentParamsMapper = ButtonComponentParamsMapper()

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: GiftCardConfiguration,
        application: Application,
        componentCallback: ComponentCallback<GiftCardComponentState>,
        order: Order?,
        key: String?,
    ): GiftCardComponent {
        assertSupported(paymentMethod)

        val clientSideEncrypter = ClientSideEncrypter()
        val dateGenerator = DateGenerator()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
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
                componentEventHandler = DefaultComponentEventHandler(),
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
        componentCallback: SessionComponentCallback<GiftCardComponentState>,
        key: String?
    ): GiftCardComponent {
        assertSupported(paymentMethod)

        val clientSideEncrypter = ClientSideEncrypter()
        val dateGenerator = DateGenerator()
        val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)
        val cardEncrypter = DefaultCardEncrypter(genericEncrypter)
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = componentParamsMapper.mapToParams(configuration, overrideComponentParams)
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

            val sessionComponentEventHandler = SessionComponentEventHandler<GiftCardComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            GiftCardComponent(
                giftCardDelegate = giftCardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, giftCardDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, GiftCardComponent::class.java]
            .also { component ->
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
        return GiftCardComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
