/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.redirect.RedirectDelegate

class Adyen3DS2ComponentProvider : ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: Adyen3DS2Configuration
    ): Adyen3DS2Component {
        val submitFingerprintRepository = SubmitFingerprintRepository()
        val adyen3DS2DetailsParser = Adyen3DS2Serializer()
        val redirectDelegate = RedirectDelegate()
        val threeDS2Factory = viewModelFactory {
            Adyen3DS2Component(
                application,
                configuration,
                submitFingerprintRepository,
                adyen3DS2DetailsParser,
                redirectDelegate
            )
        }
        return ViewModelProvider(viewModelStoreOwner, threeDS2Factory).get(Adyen3DS2Component::class.java)
    }

    override fun requiresConfiguration(): Boolean = false

    override fun requiresView(action: Action): Boolean = false

    override fun getSupportedActionTypes(): List<String> {
        return listOf(Threeds2FingerprintAction.ACTION_TYPE, Threeds2ChallengeAction.ACTION_TYPE, Threeds2Action.ACTION_TYPE)
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }
}
