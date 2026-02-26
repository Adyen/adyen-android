/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

internal data class StoredPaymentMethodsViewState(
    val cards: List<StoredPaymentMethodsListItem>,
    val others: List<StoredPaymentMethodsListItem>,
) {

    internal data class StoredPaymentMethodsListItem(
        val id: String,
        val icon: String,
        val title: String,
        val subtitle: String?,
    )
}
