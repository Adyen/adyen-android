/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/12/2023.
 */

package com.adyen.checkout.dropin.internal.provider

import android.app.Application
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.AlwaysAvailablePaymentMethod
import com.adyen.checkout.components.core.internal.NotAvailablePaymentMethod
import com.adyen.checkout.components.core.internal.PaymentMethodAvailabilityCheck
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.internal.util.runCompileOnly
import com.adyen.checkout.googlepay.internal.provider.GooglePayComponentProvider
import com.adyen.checkout.wechatpay.WeChatPayProvider

@Suppress("LongParameterList")
internal fun checkPaymentMethodAvailability(
    application: Application,
    paymentMethod: PaymentMethod,
    checkoutConfiguration: CheckoutConfiguration,
    dropInOverrideParams: DropInOverrideParams,
    callback: ComponentAvailableCallback,
) {
    try {
        adyenLog(AdyenLogLevel.VERBOSE, "checkPaymentMethodAvailability") {
            "Checking availability for type - ${paymentMethod.type}"
        }

        val type = paymentMethod.type ?: throw CheckoutException("PaymentMethod type is null")

        val availabilityCheck = getPaymentMethodAvailabilityCheck(type, dropInOverrideParams)

        availabilityCheck.isAvailable(application, paymentMethod, checkoutConfiguration, callback)
    } catch (e: CheckoutException) {
        adyenLog(AdyenLogLevel.ERROR, "checkPaymentMethodAvailability", e) {
            "Unable to initiate ${paymentMethod.type}"
        }
        callback.onAvailabilityResult(false, paymentMethod)
    }
}

/**
 * Provides the [PaymentMethodAvailabilityCheck] class for the specified [paymentMethodType], if available.
 */
private fun getPaymentMethodAvailabilityCheck(
    paymentMethodType: String,
    dropInOverrideParams: DropInOverrideParams,
): PaymentMethodAvailabilityCheck<*> {
    val availabilityCheck = when (paymentMethodType) {
        PaymentMethodTypes.GOOGLE_PAY,
        PaymentMethodTypes.GOOGLE_PAY_LEGACY -> runCompileOnly {
            GooglePayComponentProvider(dropInOverrideParams)
        }

        PaymentMethodTypes.WECHAT_PAY_SDK -> runCompileOnly { WeChatPayProvider() }
        else -> AlwaysAvailablePaymentMethod()
    }

    return availabilityCheck ?: NotAvailablePaymentMethod()
}
