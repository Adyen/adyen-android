/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */

package com.adyen.checkout.issuerlist

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType

internal object IssuerListViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            IssuerListComponentViewType.RECYCLER_VIEW -> IssuerListRecyclerView(context, attrs, defStyleAttr)
            IssuerListComponentViewType.SPINNER_VIEW -> IssuerListSpinnerView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

enum class IssuerListComponentViewType : ComponentViewType {
    RECYCLER_VIEW, SPINNER_VIEW;

    override val viewProvider: ViewProvider = IssuerListViewProvider
}
