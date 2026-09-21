/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun SecondaryScreen(
    isNested: Boolean,
    onNavigationClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        val (navigationIcon, navigationDescription) = remember {
            if (isNested) {
                Icons.AutoMirrored.Filled.ArrowBack to CheckoutLocalizationKey.GENERAL_BACK
            } else {
                Icons.Default.Close to CheckoutLocalizationKey.GENERAL_CLOSE
            }
        }

        IconButton(onClick = onNavigationClick) {
            Icon(
                imageVector = navigationIcon,
                contentDescription = resolveString(navigationDescription),
                tint = CheckoutThemeProvider.colors.primary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimensions.Spacing.Large)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}
