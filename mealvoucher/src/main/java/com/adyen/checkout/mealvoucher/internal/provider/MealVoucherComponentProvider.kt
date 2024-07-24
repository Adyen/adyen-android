/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.data.api.DefaultPublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyService
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.cse.internal.CardEncryptorFactory
import com.adyen.checkout.giftcard.internal.GiftCardComponentEventHandler
import com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentCallbackWrapper
import com.adyen.checkout.giftcard.internal.SessionsGiftCardComponentEventHandler
import com.adyen.checkout.giftcard.internal.ui.DefaultGiftCardDelegate
import com.adyen.checkout.giftcard.toCheckoutConfiguration
import com.adyen.checkout.mealvoucher.MealVoucherComponent
import com.adyen.checkout.mealvoucher.MealVoucherComponentCallback
import com.adyen.checkout.mealvoucher.MealVoucherComponentState
import com.adyen.checkout.mealvoucher.MealVoucherConfiguration
import com.adyen.checkout.mealvoucher.SessionsMealVoucherComponentCallback
import com.adyen.checkout.mealvoucher.internal.ui.MealVoucherComponentViewType
import com.adyen.checkout.mealvoucher.internal.ui.model.MealVoucherComponentParamsMapper
import com.adyen.checkout.mealvoucher.internal.util.MealVoucherValidator
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

class MealVoucherComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
// TODO remove suppress annotation
@Suppress("UnusedPrivateProperty")
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        MealVoucherComponent,
        MealVoucherConfiguration,
        MealVoucherComponentState,
        MealVoucherComponentCallback,
        >,
    SessionPaymentComponentProvider<
        MealVoucherComponent,
        MealVoucherConfiguration,
        MealVoucherComponentState,
        SessionsMealVoucherComponentCallback,
        > {
    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: MealVoucherComponentCallback,
        order: Order?,
        key: String?
    ): MealVoucherComponent {
        assertSupported(paymentMethod)

        val cardEncryptor = CardEncryptorFactory.provide()
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = MealVoucherComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val giftCardDelegate = DefaultGiftCardDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                publicKeyRepository = DefaultPublicKeyRepository(publicKeyService),
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                submitHandler = SubmitHandler(savedStateHandle),
                validator = MealVoucherValidator(),
                componentViewType = MealVoucherComponentViewType,
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            MealVoucherComponent(
                giftCardDelegate = giftCardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, giftCardDelegate),
                componentEventHandler = GiftCardComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, MealVoucherComponent::class.java]
            .also { component ->
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
        configuration: MealVoucherConfiguration,
        application: Application,
        componentCallback: MealVoucherComponentCallback,
        order: Order?,
        key: String?
    ): MealVoucherComponent {
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
        componentCallback: SessionsMealVoucherComponentCallback,
        key: String?
    ): MealVoucherComponent {
        assertSupported(paymentMethod)

        val cardEncryptor = CardEncryptorFactory.provide()
        val giftCardFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = MealVoucherComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
            val publicKeyService = PublicKeyService(httpClient)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val giftCardDelegate = DefaultGiftCardDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                analyticsManager = analyticsManager,
                publicKeyRepository = DefaultPublicKeyRepository(publicKeyService),
                componentParams = componentParams,
                cardEncryptor = cardEncryptor,
                submitHandler = SubmitHandler(savedStateHandle),
                validator = MealVoucherValidator(),
                componentViewType = MealVoucherComponentViewType,
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
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

            val sessionsGiftCardComponentEventHandler = SessionsGiftCardComponentEventHandler(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            MealVoucherComponent(
                giftCardDelegate = giftCardDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, giftCardDelegate),
                componentEventHandler = sessionsGiftCardComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, giftCardFactory)[key, MealVoucherComponent::class.java]
            .also { component ->
                val internalComponentCallback = SessionsGiftCardComponentCallbackWrapper(
                    component,
                    componentCallback,
                )
                component.observe(lifecycleOwner) {
                    component.componentEventHandler.onPaymentComponentEvent(it, internalComponentCallback)
                }
            }
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: MealVoucherConfiguration,
        application: Application,
        componentCallback: SessionsMealVoucherComponentCallback,
        key: String?
    ): MealVoucherComponent {
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

    @Suppress("UnusedPrivateMember")
    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return MealVoucherComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
