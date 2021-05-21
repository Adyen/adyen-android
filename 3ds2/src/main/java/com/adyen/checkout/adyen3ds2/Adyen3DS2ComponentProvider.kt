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
import com.adyen.checkout.adyen3ds2.model.Adyen3DS2Serializer
import com.adyen.checkout.adyen3ds2.repository.SubmitFingerprintRepository
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory

class Adyen3DS2ComponentProvider : ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: Adyen3DS2Configuration
    ): Adyen3DS2Component {
        val submitFingerprintRepository = SubmitFingerprintRepository()
        val adyen3DS2DetailsParser = Adyen3DS2Serializer()
        val threeDS2Factory = viewModelFactory {
            Adyen3DS2Component(
                application,
                configuration,
                submitFingerprintRepository,
                adyen3DS2DetailsParser
            )
        }
        return ViewModelProvider(viewModelStoreOwner, threeDS2Factory).get(Adyen3DS2Component::class.java)
    }

    override fun requiresConfiguration(): Boolean = false
}
