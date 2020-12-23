/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */
package com.adyen.checkout.wechatpay

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

class WeChatPayProvider : PaymentComponentProvider<WeChatPayComponent, WeChatPayConfiguration> {
    override operator fun get(
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: WeChatPayConfiguration
    ): WeChatPayComponent {
        val weChatFactory = viewModelFactory { WeChatPayComponent(GenericPaymentMethodDelegate(paymentMethod), configuration) }
        return ViewModelProvider(viewModelStoreOwner, weChatFactory).get(WeChatPayComponent::class.java)
    }

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: WeChatPayConfiguration,
        callback: ComponentAvailableCallback<WeChatPayConfiguration>
    ) {
        callback.onAvailabilityResult(WeChatPayUtils.isAvailable(applicationContext), paymentMethod, configuration)
    }
}