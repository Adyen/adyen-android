/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2022.
 */

package com.adyen.checkout.card.internal.ui

import android.content.Context
import com.adyen.checkout.card.internal.ui.view.CardView
import com.adyen.checkout.card.internal.ui.view.StoredCardView
import com.adyen.checkout.ui.core.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object CardViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            CardComponentViewType.DefaultCardView -> CardView(context)
            CardComponentViewType.StoredCardView -> StoredCardView(context)
            CardComponentViewType.AddressLookup -> TODO("address lookup")
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal sealed class CardComponentViewType : AmountButtonComponentViewType {
    object DefaultCardView : CardComponentViewType()
    object StoredCardView : CardComponentViewType()
    object AddressLookup : CardComponentViewType()

    override val viewProvider: ViewProvider = CardViewProvider
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}
