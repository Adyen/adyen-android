package com.adyen.checkout.googlepay

import com.adyen.checkout.googlepay.model.GooglePayParamUtils
import com.adyen.checkout.googlepay.util.AllowedCardNetworks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GooglePayParamUtilsTest {

    @Test
    fun testMasterCardToGooglePayNetworkMapping() {
        val mc = "mc"
        assertEquals(AllowedCardNetworks.MASTERCARD, GooglePayParamUtils.mapBrandToGooglePayNetwork(mc))
    }

    @Test
    fun testOtherBrandToGooglePayNetworkMapping() {
        val amex = "amex"
        assertEquals(AllowedCardNetworks.AMEX, GooglePayParamUtils.mapBrandToGooglePayNetwork(amex))
    }

    @Test
    fun testUnsupportedBrandToGooglePayNetworkMapping() {
        val maestro = "maestro"
        assertNull(GooglePayParamUtils.mapBrandToGooglePayNetwork(maestro))
    }
}
