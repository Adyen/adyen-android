/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin

import android.os.Parcelable
import com.adyen.checkout.core.common.PaymentResult
import kotlinx.parcelize.Parcelize

// TODO - KDocs
sealed interface DropInResult : Parcelable {

    @Parcelize
    data class Completed internal constructor(val result: PaymentResult) : DropInResult

    // TODO - Replace type after error propagation
    @Parcelize
    data class Failed internal constructor(val error: String) : DropInResult

    @Parcelize
    class Cancelled : DropInResult
}
