package com.adyen.checkout.card

import com.adyen.checkout.card.util.AddressFormUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressFormUtilsTest {

    @Test
    fun testMakeHouseNumberOrName() {
        val houseNumber = "12"
        val apartmentSuite = "3b"
        assertEquals("12 3b", AddressFormUtils.makeHouseNumberOrName(houseNumber, apartmentSuite))
    }

    @Test
    fun testMakeHouseNumberOrNameApartmentSuiteEmpty() {
        val houseNumber = "12"
        val apartmentSuite = ""
        assertEquals("12", AddressFormUtils.makeHouseNumberOrName(houseNumber, apartmentSuite))
    }
}
