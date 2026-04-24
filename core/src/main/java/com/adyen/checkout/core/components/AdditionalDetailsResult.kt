/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 17/4/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.error.CheckoutError

sealed interface AdditionalDetailsResult {

    data class Finished(val resultCode: String) : AdditionalDetailsResult

    data class Error(val error: CheckoutError) : AdditionalDetailsResult
}
