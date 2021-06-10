/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.qrcode

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.redirect.RedirectDelegate

class QRCodeComponentProvider : ActionComponentProvider<QRCodeComponent, QRCodeConfiguration> {
    override fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: QRCodeConfiguration
    ): QRCodeComponent {
        val redirectDelegate = RedirectDelegate()
        val qrCodeFactory = viewModelFactory {
            QRCodeComponent(
                application,
                configuration,
                redirectDelegate
            )
        }
        return ViewModelProvider(viewModelStoreOwner, qrCodeFactory).get(QRCodeComponent::class.java)
    }

    override fun requiresConfiguration(): Boolean = false
}
