/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 17/4/2026.
 */

package com.adyen.checkout.core.components

sealed interface AdditionalDetailsResult {

    /** Indicates the additional-details exchange has completed and carries a final `resultCode`. */
    data class Completion(val resultCode: String) : AdditionalDetailsResult
}
