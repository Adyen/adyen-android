/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2.internal.provider

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintRepository
import com.adyen.checkout.adyen3ds2.internal.data.api.SubmitFingerprintService
import com.adyen.checkout.adyen3ds2.internal.data.model.Adyen3DS2Serializer
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.internal.ui.DefaultAdyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.internal.ui.model.Adyen3DS2ComponentParamsMapper
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.DefaultActionComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.internal.util.AndroidBase64Encoder
import com.adyen.checkout.components.core.internal.util.get
import com.adyen.checkout.components.core.internal.util.viewModelFactory
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.ui.core.internal.DefaultRedirectHandler
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.parameters.ChallengeParameters
import kotlinx.coroutines.Dispatchers

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class Adyen3DS2ComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration, Adyen3DS2Delegate> {

    private val componentParamsMapper = Adyen3DS2ComponentParamsMapper(overrideComponentParams, overrideSessionParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: Adyen3DS2Configuration,
        callback: ActionComponentCallback,
        key: String?,
    ): Adyen3DS2Component {
        val threeDS2Factory = viewModelFactory(savedStateRegistryOwner, null) { savedStateHandle ->
            val adyen3DS2Delegate = getDelegate(configuration, savedStateHandle, application)

            Adyen3DS2Component(
                delegate = adyen3DS2Delegate,
                actionComponentEventHandler = DefaultActionComponentEventHandler(callback)
            )
        }
        return ViewModelProvider(viewModelStoreOwner, threeDS2Factory)[key, Adyen3DS2Component::class.java]
            .also { component ->
                component.observe(lifecycleOwner, component.actionComponentEventHandler::onActionComponentEvent)
            }
    }

    override fun getDelegate(
        configuration: Adyen3DS2Configuration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): Adyen3DS2Delegate {
        val componentParams = componentParamsMapper.mapToParams(
            configuration,
            null
        )
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val submitFingerprintService = SubmitFingerprintService(httpClient)
        val submitFingerprintRepository = SubmitFingerprintRepository(submitFingerprintService)
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val adyen3DS2DetailsParser = Adyen3DS2Serializer()
        val redirectHandler = DefaultRedirectHandler()
        val embeddedRequestorAppUrl = ChallengeParameters.getEmbeddedRequestorAppURL(application)
        return DefaultAdyen3DS2Delegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            componentParams = componentParams,
            submitFingerprintRepository = submitFingerprintRepository,
            paymentDataRepository = paymentDataRepository,
            adyen3DS2Serializer = adyen3DS2DetailsParser,
            redirectHandler = redirectHandler,
            threeDS2Service = ThreeDS2Service.INSTANCE,
            defaultDispatcher = Dispatchers.Default,
            embeddedRequestorAppUrl = embeddedRequestorAppUrl,
            base64Encoder = AndroidBase64Encoder(),
            application = application,
        )
    }

    override val supportedActionTypes: List<String>
        get() = listOf(
            Threeds2FingerprintAction.ACTION_TYPE,
            Threeds2ChallengeAction.ACTION_TYPE,
            Threeds2Action.ACTION_TYPE
        )

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }
}
