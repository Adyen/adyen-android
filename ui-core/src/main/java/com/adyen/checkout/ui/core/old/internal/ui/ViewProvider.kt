/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ViewProvider {
    fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView

    fun getView(
        viewType: ComponentViewType,
        layoutInflater: LayoutInflater
    ): ComponentView = getView(viewType, layoutInflater.context)
}
