/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2025.
 */

package com.adyen.checkout.card.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.OnBinLookupCallback
import com.adyen.checkout.card.OnBinValueCallback

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardCallbacks internal constructor() {
    internal var onBinValue: OnBinValueCallback? = null
    internal var onBinLookup: OnBinLookupCallback? = null
}
