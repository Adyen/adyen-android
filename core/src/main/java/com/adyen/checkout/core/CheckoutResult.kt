/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core

// TODO - Kdocs
sealed interface CheckoutResult {

    sealed interface Advanced : CheckoutResult {
        class Finished : Advanced
        class Action : Advanced
        class Error : Advanced
    }

    class Sessions : CheckoutResult
}
