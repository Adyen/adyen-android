/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.ui

import android.content.Context
import com.adyen.checkout.giftcard.internal.ui.GiftCardComponentViewType
import com.adyen.checkout.mealvoucherfr.internal.ui.view.MealVoucherFRView
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object MealVoucherFRViewProvider : ViewProvider {
    override fun getView(viewType: ComponentViewType, context: Context): ComponentView {
        return when (viewType) {
            MealVoucherFRComponentViewType -> MealVoucherFRView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object MealVoucherFRComponentViewType : GiftCardComponentViewType() {
    override val viewProvider: ViewProvider = MealVoucherFRViewProvider
}
