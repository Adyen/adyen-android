/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/1/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AddressInputModelTest {

    @Test
    fun `when set is called address input model should copy all the information from the given model`() {
        val addressInputModel = AddressInputModel()

        val addressInputModelFrom = AddressInputModel(
            postalCode = "postalCode",
            street = "street",
            city = "city",
            country = "country",
            stateOrProvince = "stateOrProvince",
            apartmentSuite = "apartmentSuite",
            houseNumberOrName = "houseNumberOrName",
        )

        addressInputModel.set(addressInputModelFrom)

        assertEquals(addressInputModelFrom, addressInputModel)
    }

    @Test
    fun `when reset is called address input model should reset all fields except country`() {
        val addressInputModel = AddressInputModel(
            postalCode = "postalCode",
            street = "street",
            city = "city",
            country = "country",
            stateOrProvince = "stateOrProvince",
            apartmentSuite = "apartmentSuite",
            houseNumberOrName = "houseNumberOrName",
        )

        addressInputModel.reset()

        assertEquals("", addressInputModel.postalCode)
        assertEquals("", addressInputModel.street)
        assertEquals("", addressInputModel.city)
        assertEquals("", addressInputModel.stateOrProvince)
        assertEquals("", addressInputModel.apartmentSuite)
        assertEquals("", addressInputModel.houseNumberOrName)
        assertEquals("country", addressInputModel.country)
    }

    @Test
    fun `when resetAll is called address input model should reset all fields`() {
        val addressInputModel = AddressInputModel(
            postalCode = "postalCode",
            street = "street",
            city = "city",
            country = "country",
            stateOrProvince = "stateOrProvince",
            apartmentSuite = "apartmentSuite",
            houseNumberOrName = "houseNumberOrName",
        )

        addressInputModel.resetAll()

        assertEquals("", addressInputModel.postalCode)
        assertEquals("", addressInputModel.street)
        assertEquals("", addressInputModel.city)
        assertEquals("", addressInputModel.stateOrProvince)
        assertEquals("", addressInputModel.apartmentSuite)
        assertEquals("", addressInputModel.houseNumberOrName)
        assertEquals("", addressInputModel.country)
    }

    @Test
    fun `when all fields are empty isEmpty should return true`() {
        val addressInputModel = AddressInputModel(
            postalCode = "",
            street = "",
            city = "",
            country = "",
            stateOrProvince = "",
            apartmentSuite = "",
            houseNumberOrName = "",
        )

        assertTrue(addressInputModel.isEmpty)
    }

    @Test
    fun `when all fields are not empty isEmpty should return false`() {
        val addressInputModel = AddressInputModel(
            postalCode = "postalCode",
            street = "street",
            city = "city",
            country = "country",
            stateOrProvince = "stateOrProvince",
            apartmentSuite = "apartmentSuite",
            houseNumberOrName = "houseNumberOrName",
        )

        assertFalse(addressInputModel.isEmpty)
    }
}
