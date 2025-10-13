/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui

import android.content.Context
import android.view.LayoutInflater
import com.adyen.checkout.card.old.internal.ui.view.CardView
import com.adyen.checkout.card.old.internal.ui.view.StoredCardView
import com.adyen.checkout.ui.core.old.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.old.internal.ui.view.AddressLookupView

internal object CardViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            is CardComponentViewType.DefaultCardView -> CardView(context)
            is CardComponentViewType.StoredCardView -> StoredCardView(context)
            is CardComponentViewType.AddressLookup -> AddressLookupView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }

    override fun getView(
        viewType: ComponentViewType,
        layoutInflater: LayoutInflater,
    ): ComponentView {
        return when (viewType) {
            is CardComponentViewType.DefaultCardView -> CardView(layoutInflater)
            is CardComponentViewType.StoredCardView -> StoredCardView(layoutInflater.context)
            is CardComponentViewType.AddressLookup -> AddressLookupView(layoutInflater.context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal sealed class CardComponentViewType : ComponentViewType {
    data object DefaultCardView : CardComponentViewType(), AmountButtonComponentViewType {
        override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
    }

    data object StoredCardView : CardComponentViewType(), AmountButtonComponentViewType {
        override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
    }

    data object AddressLookup : CardComponentViewType()

    override val viewProvider: ViewProvider = CardViewProvider
}
