/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2022.
 */

package com.adyen.checkout.card

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.adyen3ds2.Adyen3DS2ComponentViewType
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.PaymentInProgressView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.redirect.RedirectComponentViewType

internal object CardViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            CardComponentViewType -> CardView(context, attrs, defStyleAttr)
            Adyen3DS2ComponentViewType -> PaymentInProgressView(context, attrs, defStyleAttr)
            RedirectComponentViewType -> PaymentInProgressView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object CardComponentViewType : ComponentViewType
