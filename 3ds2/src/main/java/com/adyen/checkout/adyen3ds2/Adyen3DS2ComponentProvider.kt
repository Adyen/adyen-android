/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.adyen3ds2.connection.SubmitFingerprintService
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.encoding.AndroidBase64Encoder
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.redirect.handler.DefaultRedirectHandler
import com.adyen.threeds2.ThreeDS2Service
import com.adyen.threeds2.parameters.ChallengeParameters
import kotlinx.coroutines.Dispatchers

class Adyen3DS2ComponentProvider :
    ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration, Adyen3DS2Delegate> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: Adyen3DS2Configuration
    ): Adyen3DS2Component where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: Adyen3DS2Configuration,
        defaultArgs: Bundle?
    ): Adyen3DS2Component {
        val threeDS2Factory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val adyen3DS2Delegate = getDelegate(configuration, savedStateHandle, application)

            Adyen3DS2Component(
                savedStateHandle = savedStateHandle,
                application = application,
                configuration = configuration,
                adyen3DS2Delegate = adyen3DS2Delegate,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, threeDS2Factory).get(Adyen3DS2Component::class.java)
    }

    override fun getDelegate(
        configuration: Adyen3DS2Configuration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): Adyen3DS2Delegate {
        val submitFingerprintService = SubmitFingerprintService(configuration.environment)
        val submitFingerprintRepository = SubmitFingerprintRepository(submitFingerprintService)
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val adyen3DS2DetailsParser = Adyen3DS2Serializer()
        val redirectHandler = DefaultRedirectHandler()
        val embeddedRequestorAppUrl = ChallengeParameters.getEmbeddedRequestorAppURL(application)
        return DefaultAdyen3DS2Delegate(
            savedStateHandle = savedStateHandle,
            configuration = configuration,
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

    @Deprecated(
        message = "You can safely remove this method, it will always return true as all action components require " +
            "a configuration.",
        replaceWith = ReplaceWith("true")
    )
    override fun requiresConfiguration(): Boolean = true

    override fun requiresView(action: Action): Boolean = false

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun providesDetails(): Boolean {
        return true
    }
}
