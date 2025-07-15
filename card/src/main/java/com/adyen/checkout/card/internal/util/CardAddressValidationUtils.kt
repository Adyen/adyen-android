/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 11/1/2023.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.ui.model.AddressFieldPolicyParams
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressParams

internal object CardAddressValidationUtils {
    fun isAddressOptional(addressParams: AddressParams, cardType: String?): Boolean {
        return when (addressParams) {
            is AddressParams.FullAddress -> {
                (addressParams.addressFieldPolicy as? AddressFieldPolicyParams)?.isAddressOptional(
                    cardType
                )
            }
            is AddressParams.PostalCode -> {
                (addressParams.addressFieldPolicy as? AddressFieldPolicyParams)?.isAddressOptional(
                    cardType
                )
            }
            AddressParams.None -> {
                true
            }
            is AddressParams.Lookup -> {
                false
            }
        } ?: true
    }

    private fun AddressFieldPolicyParams.isAddressOptional(cardType: String?): Boolean {
        return when (this) {
            is AddressFieldPolicyParams.Optional -> {
                true
            }
            is AddressFieldPolicyParams.OptionalForCardTypes -> {
                brands.contains(cardType)
            }
            is AddressFieldPolicyParams.Required -> {
                false
            }
        }
    }
}
