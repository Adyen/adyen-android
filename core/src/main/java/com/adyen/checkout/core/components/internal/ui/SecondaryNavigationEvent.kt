/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface SecondaryNavigationEvent {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Open(
        val key: String,
    ) : SecondaryNavigationEvent

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Close : SecondaryNavigationEvent
}
