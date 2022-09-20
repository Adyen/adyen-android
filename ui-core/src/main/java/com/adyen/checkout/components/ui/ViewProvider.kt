/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/9/2022.
 */

package com.adyen.checkout.components.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.view.ComponentViewType

interface ViewProvider {
    fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentViewNew
}
