/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/3/2022.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.TextListItem

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressListItem(
    val name: String,
    val code: String,
    val selected: Boolean
) : TextListItem(name)
