/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/5/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import android.view.LayoutInflater
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonViewProvider
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.PayButton
import com.adyen.checkout.ui.core.internal.ui.view.ProcessingPaymentView

internal class GooglePayViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        GooglePayComponentViewType -> GooglePayView(context)
        PaymentInProgressViewType -> ProcessingPaymentView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }

    override fun getView(
        viewType: ComponentViewType,
        layoutInflater: LayoutInflater
    ): ComponentView = when (viewType) {
        GooglePayComponentViewType -> GooglePayView(layoutInflater)
        PaymentInProgressViewType -> ProcessingPaymentView(layoutInflater.context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal class GooglePayButtonViewProvider : ButtonViewProvider {
    override fun getButton(context: Context): PayButton = GooglePayButtonView(context)
}

internal object GooglePayComponentViewType : ButtonComponentViewType {
    override val buttonViewProvider: ButtonViewProvider get() = GooglePayButtonViewProvider()

    override val viewProvider: ViewProvider get() = GooglePayViewProvider()

    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}

internal object PaymentInProgressViewType : ComponentViewType {

    override val viewProvider: ViewProvider get() = GooglePayViewProvider()
}
