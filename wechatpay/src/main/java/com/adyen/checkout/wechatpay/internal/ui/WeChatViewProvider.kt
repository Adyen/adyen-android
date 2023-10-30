/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/10/2022.
 */

package com.adyen.checkout.wechatpay.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.PaymentInProgressView

internal object WeChatViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        WeChatComponentViewType -> PaymentInProgressView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object WeChatComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = WeChatViewProvider
}
