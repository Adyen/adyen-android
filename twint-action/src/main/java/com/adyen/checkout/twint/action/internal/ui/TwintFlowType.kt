/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/7/2024.
 */

package com.adyen.checkout.twint.action.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class TwintFlowType {

    data class Regular(val token: String) : TwintFlowType()

    data class Recurring(val token: String) : TwintFlowType()
}
