/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui

import android.content.Context
import com.adyen.checkout.twint.internal.ui.view.TwintView
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.old.internal.ui.view.ProcessingPaymentView

internal class TwintViewProvider : ViewProvider {

    override fun getView(viewType: ComponentViewType, context: Context): ComponentView = when (viewType) {
        TwintComponentViewType -> TwintView(context)
        PaymentInProgressViewType -> ProcessingPaymentView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object TwintComponentViewType : ButtonComponentViewType {

    override val viewProvider: ViewProvider get() = TwintViewProvider()

    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}

internal object PaymentInProgressViewType : ComponentViewType {

    override val viewProvider: ViewProvider get() = TwintViewProvider()
}
