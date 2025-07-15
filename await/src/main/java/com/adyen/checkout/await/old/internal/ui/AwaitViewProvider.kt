/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/9/2022.
 */

package com.adyen.checkout.await.old.internal.ui

import android.content.Context
import com.adyen.checkout.await.old.internal.ui.view.AwaitView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object AwaitViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            AwaitComponentViewType -> AwaitView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object AwaitComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = AwaitViewProvider
}
