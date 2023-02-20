/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.internal.provider

import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.await.AwaitComponent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.qrcode.QRCodeComponent
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.voucher.VoucherComponent
import com.adyen.checkout.wechatpay.WeChatPayActionComponent

/**
 * @param action The action to be handled
 *
 * @return The provider able to handle the action.
 */
internal fun getActionProviderFor(
    action: Action
): ActionComponentProvider<*, *, *>? {
    val allActionProviders = listOf(
        RedirectComponent.PROVIDER,
        Adyen3DS2Component.PROVIDER,
        WeChatPayActionComponent.PROVIDER,
        AwaitComponent.PROVIDER,
        QRCodeComponent.PROVIDER,
        VoucherComponent.PROVIDER
    )
    return allActionProviders.firstOrNull { it.canHandleAction(action) }
}
