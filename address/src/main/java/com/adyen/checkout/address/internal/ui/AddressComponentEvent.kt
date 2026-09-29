/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.address.internal.ui.model.AddressModel

/**
 * What the address component reports to the component hosting the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface AddressComponentEvent {

    /**
     * The shopper confirmed a valid address. The host keeps it: the address component does not remember confirmed
     * addresses, and forgets unconfirmed edits on the next [AddressComponent.prefill].
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Confirmed(val address: AddressModel) : AddressComponentEvent
}
