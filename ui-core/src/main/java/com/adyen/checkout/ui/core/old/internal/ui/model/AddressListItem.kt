/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.old.internal.ui.TextListItem

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressListItem(
    val name: String,
    val code: String,
    val selected: Boolean
) : TextListItem(name)
