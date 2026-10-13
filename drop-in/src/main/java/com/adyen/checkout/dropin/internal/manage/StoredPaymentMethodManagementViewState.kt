/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/10/2026.
 */

package com.adyen.checkout.dropin.internal.manage

internal data class StoredPaymentMethodManagementViewState(
    val cards: List<ListItem>,
    val others: List<ListItem>,
) {

    internal data class ListItem(
        val id: String,
        val icon: String,
        val title: String,
        val subtitle: String?,
    )
}
