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
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.redirect.RedirectDelegate

private val VIEWABLE_PAYMENT_METHODS = listOf(PaymentMethodTypes.PIX)

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
        return when {
            !supportedActionTypes.contains(action.type) -> false
            requiresView(action) -> true // viewable action, can be handled
            !(action as? QrCodeAction)?.url.isNullOrEmpty() -> true // QR code actions that contain a url are handled as a redirect action
            else -> false
        }
    }

    override fun requiresView(action: Action): Boolean {
        return VIEWABLE_PAYMENT_METHODS.contains(action.paymentMethodType)
    }
}
