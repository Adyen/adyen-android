/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.bacs.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bacs.getBacsDirectDebitConfiguration
import com.adyen.checkout.bacs.internal.ui.DefaultBacsDirectDebitDelegate
import com.adyen.checkout.bacs.toCheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.DefaultComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.internal.SessionComponentEventHandler
import com.adyen.checkout.sessions.core.internal.SessionInteractor
import com.adyen.checkout.sessions.core.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.api.SessionService
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler

class BacsDirectDebitComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    private val dropInOverrideParams: DropInOverrideParams? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) :
    PaymentComponentProvider<
        BacsDirectDebitComponent,
        BacsDirectDebitConfiguration,
        BacsDirectDebitComponentState,
        ComponentCallback<BacsDirectDebitComponentState>,
        >,
    SessionPaymentComponentProvider<
        BacsDirectDebitComponent,
        BacsDirectDebitConfiguration,
        BacsDirectDebitComponentState,
        SessionComponentCallback<BacsDirectDebitComponentState>,
        > {

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BacsDirectDebitComponentState>,
        order: Order?,
        key: String?
    ): BacsDirectDebitComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getBacsDirectDebitConfiguration(),
            )

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = null,
            )

            val bacsDelegate = DefaultBacsDirectDebitDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
            )

            val genericActionDelegate =
                GenericActionComponentProvider(analyticsManager, dropInOverrideParams).getDelegate(
                    checkoutConfiguration = checkoutConfiguration,
                    savedStateHandle = savedStateHandle,
                    application = application,
                )

            BacsDirectDebitComponent(
                bacsDelegate = bacsDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, bacsDelegate),
                componentEventHandler = DefaultComponentEventHandler(),
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BacsDirectDebitComponent::class.java]
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
        configuration: BacsDirectDebitConfiguration,
        application: Application,
        componentCallback: ComponentCallback<BacsDirectDebitComponentState>,
        order: Order?,
        key: String?
    ): BacsDirectDebitComponent {
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
        componentCallback: SessionComponentCallback<BacsDirectDebitComponentState>,
        key: String?
    ): BacsDirectDebitComponent {
        assertSupported(paymentMethod)

        val genericFactory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,
                deviceLocale = localeProvider.getLocale(application),
                dropInOverrideParams = dropInOverrideParams,
                componentSessionParams = SessionParamsFactory.create(checkoutSession),
                componentConfiguration = checkoutConfiguration.getBacsDirectDebitConfiguration(),
            )

            val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)

            val analyticsManager = analyticsManager ?: AnalyticsManagerFactory().provide(
                componentParams = componentParams,
                application = application,
                source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
                sessionId = checkoutSession.sessionSetupResponse.id,
            )

            val bacsDelegate = DefaultBacsDirectDebitDelegate(
                observerRepository = PaymentObserverRepository(),
                componentParams = componentParams,
                paymentMethod = paymentMethod,
                order = checkoutSession.order,
                analyticsManager = analyticsManager,
                submitHandler = SubmitHandler(savedStateHandle),
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
            val sessionComponentEventHandler = SessionComponentEventHandler<BacsDirectDebitComponentState>(
                sessionInteractor = sessionInteractor,
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            )

            BacsDirectDebitComponent(
                bacsDelegate = bacsDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, bacsDelegate),
                componentEventHandler = sessionComponentEventHandler,
            )
        }

        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, BacsDirectDebitComponent::class.java]
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
        configuration: BacsDirectDebitConfiguration,
        application: Application,
        componentCallback: SessionComponentCallback<BacsDirectDebitComponentState>,
        key: String?
    ): BacsDirectDebitComponent {
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
        return BacsDirectDebitComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
