/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/11/2026.
 */

package com.adyen.checkout.card.internal.ui.model

internal enum class CardStorePaymentMethodMode {
    /**
     * The store payment method field is hidden and storePaymentMethod is not sent.
     */
    HIDDEN,

    /**
     * The store payment method field is shown and the shopper's choice is sent.
     */
    ASK_FOR_CONSENT,

    /**
     * The store payment method field is hidden and storePaymentMethod is always sent as true.
     */
    ALWAYS,
}
