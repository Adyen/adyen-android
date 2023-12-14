/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressLookupInputData(
    var query: String = ""
) {
    fun reset() {
        query = ""
    }
}
