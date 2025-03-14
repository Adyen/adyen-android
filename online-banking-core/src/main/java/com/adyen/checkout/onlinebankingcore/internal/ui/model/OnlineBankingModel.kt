/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 9/9/2022.
 */

package com.adyen.checkout.onlinebankingcore.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.TextListItem

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class OnlineBankingModel(
    val id: String,
    val name: String
) : TextListItem(name)
