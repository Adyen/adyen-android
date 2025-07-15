/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.upi.R
import com.adyen.checkout.upi.internal.ui.view.UPIView

internal object UPIViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        UPIComponentViewType -> UPIView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object UPIComponentViewType : ButtonComponentViewType {

    override val viewProvider: ViewProvider = UPIViewProvider

    override val buttonTextResId: Int = R.string.checkout_upi_continue
}
