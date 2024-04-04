/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2022.
 */

package com.adyen.checkout.action.core.internal.ui

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.internal.provider.Adyen3DS2ComponentProvider
import com.adyen.checkout.await.internal.provider.AwaitComponentProvider
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.action.BaseThreeds2Action
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LocaleProvider
import com.adyen.checkout.qrcode.internal.provider.QRCodeComponentProvider
import com.adyen.checkout.redirect.internal.provider.RedirectComponentProvider
import com.adyen.checkout.voucher.internal.provider.VoucherComponentProvider
import com.adyen.checkout.wechatpay.internal.provider.WeChatPayActionComponentProvider

internal class ActionDelegateProvider(
    private val analyticsManager: AnalyticsManager?,
    private val dropInOverrideParams: DropInOverrideParams?,
    private val localeProvider: LocaleProvider = LocaleProvider(),
) {

    fun getDelegate(
        action: Action,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): ActionDelegate {
        val provider = when (action) {
            is AwaitAction -> AwaitComponentProvider(analyticsManager, dropInOverrideParams, localeProvider)
            is QrCodeAction -> QRCodeComponentProvider(dropInOverrideParams, localeProvider)
            is RedirectAction -> RedirectComponentProvider(dropInOverrideParams, localeProvider)
            is BaseThreeds2Action -> Adyen3DS2ComponentProvider(analyticsManager, dropInOverrideParams, localeProvider)
            is VoucherAction -> VoucherComponentProvider(dropInOverrideParams, localeProvider)
            is SdkAction<*> -> WeChatPayActionComponentProvider(dropInOverrideParams, localeProvider)
            else -> throw CheckoutException("Can't find delegate for action: ${action.type}")
        }

        return provider.getDelegate(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            application = application,
        )
    }
}
