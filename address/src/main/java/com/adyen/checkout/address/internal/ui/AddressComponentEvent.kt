/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.address.internal.ui.model.AddressModel

/**
 * What the address component reports to the component hosting the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface AddressComponentEvent {

    /** The shopper saved a valid address and the form closed. The host keeps it for [AddressComponent.show]. */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Confirmed(val address: AddressModel) : AddressComponentEvent
}
