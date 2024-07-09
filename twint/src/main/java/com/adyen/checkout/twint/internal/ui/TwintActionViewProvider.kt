/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import android.content.Context
import android.view.LayoutInflater
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object TwintActionViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        TwintActionComponentViewType -> TwintActionView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }

    override fun getView(
        viewType: ComponentViewType,
        layoutInflater: LayoutInflater
    ): ComponentView = when (viewType) {
        TwintActionComponentViewType -> TwintActionView(layoutInflater)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object TwintActionComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = TwintActionViewProvider
}
