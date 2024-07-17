/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/7/2024.
 */

package com.adyen.checkout.mealvoucher.internal.ui

import android.content.Context
import com.adyen.checkout.giftcard.internal.ui.GiftCardComponentViewType
import com.adyen.checkout.mealvoucher.internal.ui.view.MealVoucherView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object MealVoucherViewProvider : ViewProvider {
    override fun getView(viewType: ComponentViewType, context: Context): ComponentView {
        return when (viewType) {
            MealVoucherComponentViewType -> MealVoucherView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object MealVoucherComponentViewType : GiftCardComponentViewType() {
    override val viewProvider: ViewProvider = MealVoucherViewProvider

    // TODO update button text if necessary
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}
