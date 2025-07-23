/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme
import com.adyen.checkout.ui.theme.AdyenColors

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ThemePreviewParameterProvider : PreviewParameterProvider<CheckoutTheme> {

    override val values = sequenceOf(
        CheckoutTheme(colors = AdyenColors.light()),
        CheckoutTheme(colors = AdyenColors.dark()),
    )
}
