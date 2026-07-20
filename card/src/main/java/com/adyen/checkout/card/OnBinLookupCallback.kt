/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutCallbacks

/**
 * Callback invoked when a BIN lookup completes for the card number entered by the shopper.
 *
 * It fires once the shopper has entered at least 11 digits and the SDK receives a `/binLookup` response (including
 * responses served from the SDK's internal cache), exposing all detected brands without any SDK-side filtering. It
 * does not fire for the client-side regex-based brand detection used while fewer digits are entered.
 *
 * Register it through [CheckoutCallbacks.card] on your [CheckoutCallbacks].
 */
fun interface OnBinLookupCallback : CheckoutAdditionalCallback {

    /**
     * Called when a BIN lookup completes.
     *
     * @param data The result of the BIN lookup, containing the issuing country and the detected brands.
     */
    fun onBinLookup(data: BinLookupData)
}
