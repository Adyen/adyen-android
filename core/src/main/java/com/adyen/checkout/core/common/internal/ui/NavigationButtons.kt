/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/9/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun NavigationBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationButton(
        icon = R.drawable.ic_navigation_back,
        localizationKey = CheckoutLocalizationKey.GENERAL_BACK,
        onClick = onClick,
        modifier = modifier,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun NavigationCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationButton(
        icon = R.drawable.ic_navigation_close,
        localizationKey = CheckoutLocalizationKey.GENERAL_CLOSE,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun NavigationButton(
    @DrawableRes icon: Int,
    localizationKey: CheckoutLocalizationKey,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = resolveString(localizationKey),
            tint = CheckoutThemeProvider.colors.primary,
        )
    }
}
