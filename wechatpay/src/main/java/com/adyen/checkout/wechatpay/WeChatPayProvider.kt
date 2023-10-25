/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/10/2023.
 */

package com.adyen.checkout.wechatpay

import android.app.Application
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentMethodAvailabilityCheck
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * This class is not an actual provider, it only checks whether WeChatPay is available.
 * There is no viewable Payment Component for WeChatPay, only an Action Component.
 * You can directly call /payments after you receive a callback from [isAvailable].
 * You can use [WeChatPayActionComponent] to handle the returned action.
 */
class WeChatPayProvider : PaymentMethodAvailabilityCheck<Configuration> {

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: Configuration?,
        callback: ComponentAvailableCallback
    ) {
        callback.onAvailabilityResult(isAvailable(applicationContext), paymentMethod)
    }

    private fun isAvailable(applicationContext: Application?): Boolean {
        val api = WXAPIFactory.createWXAPI(applicationContext, null, true)
        val isAppInstalled = api.isWXAppInstalled
        val isSupported = Build.PAY_SUPPORTED_SDK_INT <= api.wxAppSupportAPI
        api.detach()
        return isAppInstalled && isSupported
    }
}
