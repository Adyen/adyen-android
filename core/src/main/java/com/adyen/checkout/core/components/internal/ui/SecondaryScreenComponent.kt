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
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SecondaryScreenComponent {

    val navigation: Flow<SecondaryNavigationEvent>

    /**
     * Components that open their own secondary screens on this component's back stack, such as a form shown on one of
     * this component's screens. The host follows their [navigation] too, and each screen is rendered by the component
     * that opened it. Keep the list fixed: the back stack survives recreation and refers to it by position.
     */
    val childScreenComponents: List<SecondaryScreenComponent>
        get() = emptyList()

    @Composable
    fun SecondaryContent(
        identifier: String,
        modifier: Modifier
    )
}
