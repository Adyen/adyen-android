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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.ui.NavigationBackButton
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun SecondaryScreen(
    isNested: Boolean,
    onNavigationClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        if (isNested) {
            NavigationBackButton(
                onClick = onNavigationClick,
            )
        } else {
            NavigationCloseButton(
                onClick = onNavigationClick,
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
