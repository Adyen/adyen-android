/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.giftcard.R
import com.adyen.checkout.giftcard.internal.ui.view.GiftCardView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object GiftCardViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            is GiftCardComponentViewType -> GiftCardView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class GiftCardComponentViewType : ButtonComponentViewType {
    override val viewProvider: ViewProvider = GiftCardViewProvider
    override val buttonTextResId: Int = R.string.checkout_giftcard_redeem_button
}
