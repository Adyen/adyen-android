/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/5/2021.
 */

package com.adyen.checkout.wechatpay

import android.app.Application
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

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
        callback: ComponentAvailableCallback<Configuration>
    ) {
        callback.onAvailabilityResult(WeChatPayUtils.isAvailable(applicationContext), paymentMethod, configuration)
    }
}
