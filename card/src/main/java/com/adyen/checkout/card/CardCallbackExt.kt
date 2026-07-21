/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.components.CheckoutCallbacks

/**
 * Registers card-specific callbacks on this [CheckoutCallbacks].
 *
 * @param onBinChange Called when the first digits (BIN) of the card number change. See [OnBinChangeCallback].
 * @param onBinLookup Called when a BIN lookup completes for the entered card number. See [OnBinLookupCallback].
 */
@JvmOverloads
fun CheckoutCallbacks.card(
    onBinChange: OnBinChangeCallback? = null,
    onBinLookup: OnBinLookupCallback? = null,
) {
    onBinChange?.let { addAdditionalCallback(it) }
    onBinLookup?.let { addAdditionalCallback(it) }
}
