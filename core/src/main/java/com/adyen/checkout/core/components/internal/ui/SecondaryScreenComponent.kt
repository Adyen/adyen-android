/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/4/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SecondaryScreenComponent {

    @Composable
    fun SecondaryContent(
        identifier: String,
        modifier: Modifier
    )
}
