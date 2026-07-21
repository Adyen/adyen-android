/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/07/2026.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutCallbacks

/**
 * Callback invoked when the first digits (BIN) of the card number entered by the shopper change.
 *
 * Register it through [CheckoutCallbacks.card] on your [CheckoutCallbacks].
 */
fun interface OnBinChangeCallback : CheckoutAdditionalCallback {

    /**
     * Called when the BIN of the entered card number changes.
     *
     * @param binValue The current BIN (the leading digits of the card number).
     */
    fun onBinChange(binValue: String)
}
