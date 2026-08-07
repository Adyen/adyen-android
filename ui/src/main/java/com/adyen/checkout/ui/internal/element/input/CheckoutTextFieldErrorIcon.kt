/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/8/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

/**
 * The generic trailing icon of a [CheckoutTextField] that is showing an error.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutTextFieldErrorIcon() {
    Icon(
        modifier = Modifier.size(Dimensions.LogoSize.small),
        imageVector = ImageVector.vectorResource(R.drawable.ic_warning),
        contentDescription = null,
        tint = CheckoutThemeProvider.colors.destructive,
    )
}
