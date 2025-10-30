/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/9/2022.
 */

package com.adyen.checkout.adyen3ds2.old.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.old.internal.ui.view.PaymentInProgressView

internal object Adyen3DS2ViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        Adyen3DS2ComponentViewType -> PaymentInProgressView(context)
        else -> error("Unsupported view type")
    }
}

internal object Adyen3DS2ComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = Adyen3DS2ViewProvider
}
