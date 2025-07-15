/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui

import android.content.Context
import com.adyen.checkout.issuerlist.internal.ui.view.IssuerListRecyclerView
import com.adyen.checkout.issuerlist.internal.ui.view.IssuerListSpinnerView
import com.adyen.checkout.ui.core.old.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object IssuerListViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            IssuerListComponentViewType.RecyclerView -> IssuerListRecyclerView(context)
            IssuerListComponentViewType.SpinnerView -> IssuerListSpinnerView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal sealed class IssuerListComponentViewType(
    override val viewProvider: ViewProvider = IssuerListViewProvider
) : ComponentViewType {
    object RecyclerView : IssuerListComponentViewType()
    object SpinnerView : IssuerListComponentViewType(), AmountButtonComponentViewType {
        override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
    }
}
