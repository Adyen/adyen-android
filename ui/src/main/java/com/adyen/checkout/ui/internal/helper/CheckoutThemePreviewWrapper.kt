/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/4/2026.
 */

package com.adyen.checkout.ui.internal.helper

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.internal.theme.toCompose
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A wrapper that provides a preview environment for a [CheckoutTheme].
 * This should only be used in compose previews.
 */
@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun CheckoutThemePreviewWrapper(
    theme: CheckoutTheme,
    content: @Composable () -> Unit
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Spacing.Large),
        ) {
            content()
        }
    }
}
