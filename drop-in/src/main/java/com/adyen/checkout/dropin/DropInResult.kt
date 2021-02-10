/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/2/2021.
 */

package com.adyen.checkout.dropin

sealed class DropInResult {
    class CancelledByUser : DropInResult()
    class Error(val reason: String?) : DropInResult()
    class Finished(val result: String) : DropInResult()
}
