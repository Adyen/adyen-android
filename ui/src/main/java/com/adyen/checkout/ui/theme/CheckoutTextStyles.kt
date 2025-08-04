/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.internal.DefaultTextStyles

// TODO - Add KDocs
@Immutable
data class CheckoutTextStyle(
    val size: Int,
    val weight: Int,
    val lineHeight: Int,
    val fontResId: Int?,
)

@Immutable
data class CheckoutTextStyles(
    val title: CheckoutTextStyle,
    val subtitle: CheckoutTextStyle,
    val body: CheckoutTextStyle,
    val bodyEmphasized: CheckoutTextStyle,
    val subHeadline: CheckoutTextStyle,
    val subHeadlineEmphasized: CheckoutTextStyle,
    val footnote: CheckoutTextStyle,
    val footnoteEmphasized: CheckoutTextStyle,
) {

    companion object {

        @Suppress("LongParameterList")
        fun default(
            title: CheckoutTextStyle = DefaultTextStyles.Title,
            subtitle: CheckoutTextStyle = DefaultTextStyles.Subtitle,
            body: CheckoutTextStyle = DefaultTextStyles.Body,
            bodyEmphasized: CheckoutTextStyle = DefaultTextStyles.BodyEmphasized,
            subHeadline: CheckoutTextStyle = DefaultTextStyles.SubHeadline,
            subHeadlineEmphasized: CheckoutTextStyle = DefaultTextStyles.SubHeadlineEmphasized,
            footnote: CheckoutTextStyle = DefaultTextStyles.Footnote,
            footnoteEmphasized: CheckoutTextStyle = DefaultTextStyles.FootnoteEmphasized,
        ) = CheckoutTextStyles(
            title = title,
            subtitle = subtitle,
            body = body,
            bodyEmphasized = bodyEmphasized,
            subHeadline = subHeadline,
            subHeadlineEmphasized = subHeadlineEmphasized,
            footnote = footnote,
            footnoteEmphasized = footnoteEmphasized,
        )
    }
}
