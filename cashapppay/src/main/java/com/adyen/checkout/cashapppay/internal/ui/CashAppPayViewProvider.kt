/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import android.content.Context
import com.adyen.checkout.cashapppay.internal.ui.view.CashAppPayButtonView
import com.adyen.checkout.cashapppay.internal.ui.view.CashAppPayView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonViewProvider
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.PayButton
import com.adyen.checkout.ui.core.internal.ui.view.ProcessingPaymentView

internal object CashAppPayViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        CashAppPayComponentViewType -> CashAppPayView(context)
        PaymentInProgressViewType -> ProcessingPaymentView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal class CashAppPayButtonViewProvider : ButtonViewProvider {
    override fun getButton(context: Context): PayButton =
        CashAppPayButtonView(context)
}

internal object CashAppPayComponentViewType : ButtonComponentViewType {

    override val buttonViewProvider: ButtonViewProvider get() = CashAppPayButtonViewProvider()

    override val viewProvider: ViewProvider = CashAppPayViewProvider

    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}

internal object PaymentInProgressViewType : ComponentViewType {
    override val viewProvider: ViewProvider = CashAppPayViewProvider
}
