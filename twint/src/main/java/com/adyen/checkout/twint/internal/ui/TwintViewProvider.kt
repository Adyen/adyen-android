/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.PaymentInProgressView

internal object TwintViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        TwintComponentViewType -> PaymentInProgressView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object TwintComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = TwintViewProvider
}
