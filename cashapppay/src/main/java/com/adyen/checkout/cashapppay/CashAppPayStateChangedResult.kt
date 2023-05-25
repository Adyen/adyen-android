/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/5/2023.
 */

package com.adyen.checkout.cashapppay

import com.adyen.checkout.core.exception.ComponentException

internal sealed class CashAppPayStateChangedResult {
    data class Success(val outputData: CashAppPayOutputData) : CashAppPayStateChangedResult()
    data class Error(val componentException: ComponentException) : CashAppPayStateChangedResult()
    object NoOps : CashAppPayStateChangedResult()
}
