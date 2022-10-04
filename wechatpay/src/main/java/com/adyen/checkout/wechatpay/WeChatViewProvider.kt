/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/10/2022.
 */

package com.adyen.checkout.wechatpay

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.PaymentInProgressView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType

internal object WeChatViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentViewNew = when (viewType) {
        WeChatComponentViewType -> PaymentInProgressView(context, attrs, defStyleAttr)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object WeChatComponentViewType : ComponentViewType
