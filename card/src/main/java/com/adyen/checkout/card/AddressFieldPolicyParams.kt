/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 6/1/2023.
 */

package com.adyen.checkout.card

import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AddressFieldPolicyParams : AddressFieldPolicy {
    /**
     * Address form fields will be required.
     */
    @Parcelize
    object Required : AddressFieldPolicyParams()

    /**
     * Address form fields will be optional.
     */
    @Parcelize
    object Optional : AddressFieldPolicyParams()

    /**
     * Address form fields will be optional for given [brands] and required for the other brands.
     */
    @Parcelize
    data class OptionalForCardTypes(val brands: List<String>) : AddressFieldPolicyParams()
}
