/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 11/1/2023.
 */

package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressParams
import com.adyen.checkout.card.AddressFieldPolicyParams
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

internal class CardAddressValidationUtilsTest {

    @Test
    fun `when address params is None, isAddressOptional should return true`() {
        val addressParams = AddressParams.None

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, null)

        assertTrue(isOptional)
    }

    @Test
    fun `when address params is PostalCode and addressPolicy is Required, isAddressOptional should return false`() {
        val addressParams = AddressParams.PostalCode(addressFieldPolicy = AddressFieldPolicyParams.Required)

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, null)

        assertFalse(isOptional)
    }

    @Test
    fun `when address params is PostalCode and addressPolicy is Optional, isAddressOptional should return true`() {
        val addressParams = AddressParams.PostalCode(addressFieldPolicy = AddressFieldPolicyParams.Optional)

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, null)

        assertTrue(isOptional)
    }

    @Test
    fun `when address params is PostalCode, addressPolicy is OptionalForCardTypes with ms and detected cars is ms, isAddressOptional should return true`() {
        val cardType = "ms"
        val addressParams = AddressParams.PostalCode(
            addressFieldPolicy = AddressFieldPolicyParams.OptionalForCardTypes(
                brands = listOf(cardType)
            )
        )

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, cardType)

        assertTrue(isOptional)
    }

    @Test
    fun `when address params is PostalCode, addressPolicy is OptionalForCardTypes with ms and detected cars is vs, isAddressOptional should return false`() {
        val cardType = "ms"
        val detectedCardType = "vs"
        val addressParams = AddressParams.PostalCode(
            addressFieldPolicy = AddressFieldPolicyParams.OptionalForCardTypes(
                brands = listOf(cardType)
            )
        )

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, detectedCardType)

        assertFalse(isOptional)
    }

    @Test
    fun `when address params is FullAddress and addressPolicy is Required, isAddressOptional should return false`() {
        val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Required)

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, null)

        assertFalse(isOptional)
    }

    @Test
    fun `when address params is FullAddress and addressPolicy is Optional, isAddressOptional should return true`() {
        val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Optional)

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, null)

        assertTrue(isOptional)
    }

    @Test
    fun `when address params is FullAddress, addressPolicy is OptionalForCardTypes with ms and detected cars is ms, isAddressOptional should return true`() {
        val cardType = "ms"
        val addressParams = AddressParams.FullAddress(
            addressFieldPolicy = AddressFieldPolicyParams.OptionalForCardTypes(
                brands = listOf(cardType)
            )
        )

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, cardType)

        assertTrue(isOptional)
    }

    @Test
    fun `when address params is FullAddress, addressPolicy is OptionalForCardTypes with ms and detected cars is vs, isAddressOptional should return false`() {
        val cardType = "ms"
        val detectedCardType = "vs"
        val addressParams = AddressParams.FullAddress(
            addressFieldPolicy = AddressFieldPolicyParams.OptionalForCardTypes(
                brands = listOf(cardType)
            )
        )

        val isOptional = CardAddressValidationUtils.isAddressOptional(addressParams, detectedCardType)

        assertFalse(isOptional)
    }
}
