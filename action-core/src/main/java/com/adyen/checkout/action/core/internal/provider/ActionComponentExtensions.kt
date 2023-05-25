/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.core.internal.provider

import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.await.AwaitComponent
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.qrcode.QRCodeComponent
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.voucher.VoucherComponent
import com.adyen.checkout.wechatpay.WeChatPayActionComponent

private val allActionProviders = listOfNotNull(
    runCompileOnly { Adyen3DS2Component.PROVIDER },
    runCompileOnly { AwaitComponent.PROVIDER },
    runCompileOnly { QRCodeComponent.PROVIDER },
    runCompileOnly { RedirectComponent.PROVIDER },
    runCompileOnly { VoucherComponent.PROVIDER },
    runCompileOnly { WeChatPayActionComponent.PROVIDER },
)

/**
 * @param action The action to be handled
 *
 * @return The provider able to handle the action.
 */
internal fun getActionProviderFor(
    action: Action
): ActionComponentProvider<*, *, *>? {
    return allActionProviders.firstOrNull { it.canHandleAction(action) }
}
