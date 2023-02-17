/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ButtonComponentViewType
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.giftcard.R
import com.adyen.checkout.giftcard.internal.ui.view.GiftCardView

internal object GiftCardViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            GiftCardComponentViewType -> GiftCardView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object GiftCardComponentViewType : ButtonComponentViewType {
    override val viewProvider: ViewProvider = GiftCardViewProvider
    override val buttonTextResId: Int = R.string.checkout_giftcard_redeem_button
}
