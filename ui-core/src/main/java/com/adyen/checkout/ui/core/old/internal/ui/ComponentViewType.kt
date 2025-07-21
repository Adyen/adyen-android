/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import com.adyen.checkout.ui.core.R

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentViewType {
    val viewProvider: ViewProvider
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ButtonComponentViewType : ComponentViewType {

    val buttonViewProvider: ButtonViewProvider get() = DefaultButtonViewProvider()

    val buttonTextResId: Int
        @StringRes get

    companion object {
        val DEFAULT_BUTTON_TEXT_RES_ID = R.string.pay_button
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AmountButtonComponentViewType : ButtonComponentViewType
