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
data class AdyenTextStyle(
    val size: Int,
    val weight: Int,
    val lineHeight: Int,
    val fontResId: Int?,
)

@Immutable
data class AdyenTextStyles(
    val title: AdyenTextStyle,
    val subtitle: AdyenTextStyle,
    val body: AdyenTextStyle,
    val bodyEmphasized: AdyenTextStyle,
    val subHeadline: AdyenTextStyle,
    val subHeadlineEmphasized: AdyenTextStyle,
    val footnote: AdyenTextStyle,
    val footnoteEmphasized: AdyenTextStyle,
) {

    companion object {

        @Suppress("LongParameterList")
        fun default(
            title: AdyenTextStyle = DefaultTextStyles.Title,
            subtitle: AdyenTextStyle = DefaultTextStyles.Subtitle,
            body: AdyenTextStyle = DefaultTextStyles.Body,
            bodyEmphasized: AdyenTextStyle = DefaultTextStyles.BodyEmphasized,
            subHeadline: AdyenTextStyle = DefaultTextStyles.SubHeadline,
            subHeadlineEmphasized: AdyenTextStyle = DefaultTextStyles.SubHeadlineEmphasized,
            footnote: AdyenTextStyle = DefaultTextStyles.Footnote,
            footnoteEmphasized: AdyenTextStyle = DefaultTextStyles.FootnoteEmphasized,
        ) = AdyenTextStyles(
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
