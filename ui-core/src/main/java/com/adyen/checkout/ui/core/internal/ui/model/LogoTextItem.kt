/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/10/2024.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import com.adyen.checkout.core.old.Environment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface LogoTextItem {
    fun getViewType(): LogoTextItemViewType

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class LogoItem(
        val logoPath: String,
        // We need the environment to load the logo
        val environment: Environment,
    ) : LogoTextItem {
        override fun getViewType() = LogoTextItemViewType.Logo
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class TextItem(
        @StringRes
        val textResId: Int,
    ) : LogoTextItem {
        override fun getViewType() = LogoTextItemViewType.Text
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    enum class LogoTextItemViewType(val type: Int) {
        Logo(0),
        Text(1)
    }
}
