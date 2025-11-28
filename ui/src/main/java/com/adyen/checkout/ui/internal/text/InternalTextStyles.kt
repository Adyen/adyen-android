/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/11/2025.
 */

package com.adyen.checkout.ui.internal.text

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.theme.CheckoutTextStyles

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalTextStyles(
    val title: InternalTextStyle,
    val subtitle: InternalTextStyle,
    val body: InternalTextStyle,
    val bodyEmphasized: InternalTextStyle,
    val subHeadline: InternalTextStyle,
    val subHeadlineEmphasized: InternalTextStyle,
    val footnote: InternalTextStyle,
    val footnoteEmphasized: InternalTextStyle,
) {

    internal companion object {

        fun from(textStyles: CheckoutTextStyles): InternalTextStyles {
            val font = textStyles.font
            return InternalTextStyles(
                title = TextStyleDefaults.title(font),
                subtitle = TextStyleDefaults.subtitle(font),
                body = TextStyleDefaults.body(font),
                bodyEmphasized = TextStyleDefaults.bodyEmphasized(font),
                subHeadline = TextStyleDefaults.subHeadline(font),
                subHeadlineEmphasized = TextStyleDefaults.subHeadlineEmphasized(font),
                footnote = TextStyleDefaults.footnote(font),
                footnoteEmphasized = TextStyleDefaults.footnoteEmphasized(font),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalTextStyle(
    val size: Int,
    val weight: Int,
    val lineHeight: Int,
    val fontResId: Int?,
)
