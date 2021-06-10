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
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
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

    override fun getSupportedActionTypes(): List<String> {
        return listOf(QrCodeAction.ACTION_TYPE)
    }

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type)
    }

    override fun requiresView(action: Action): Boolean {
        //QR code actions that contain a url are actually redirect actions
        return (action is QrCodeAction && action.url != null)
    }
}
