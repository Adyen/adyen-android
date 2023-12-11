/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/12/2023.
 */

package com.adyen.checkout.card.internal.ui.model

internal data class AddressLookupInputData(
    var query: String = "",
    var isManualEntryMode: Boolean = false,
    var isLoading: Boolean = false,
) {
    fun reset() {
        query = ""
        isManualEntryMode = false
        isLoading = false
    }
}
