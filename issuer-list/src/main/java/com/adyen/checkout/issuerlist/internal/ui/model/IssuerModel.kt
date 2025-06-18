/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/11/2020.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.Environment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class IssuerModel(
    val id: String,
    val name: String,
    // We need the environment to load the logo
    val environment: Environment,
)
