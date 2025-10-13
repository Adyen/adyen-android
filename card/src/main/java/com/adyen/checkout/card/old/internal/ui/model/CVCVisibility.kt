/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CVCVisibility {
    ALWAYS_SHOW,
    HIDE_FIRST,
    ALWAYS_HIDE
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class StoredCVCVisibility {
    SHOW,
    HIDE
}
