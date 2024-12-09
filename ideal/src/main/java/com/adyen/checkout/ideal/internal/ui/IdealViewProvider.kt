/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2024.
 */

package com.adyen.checkout.ideal.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.ProcessingPaymentView

internal class IdealViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context
    ): ComponentView = when (viewType) {
        PaymentInProgressViewType -> ProcessingPaymentView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object PaymentInProgressViewType : ComponentViewType {

    override val viewProvider: ViewProvider get() = IdealViewProvider()
}
