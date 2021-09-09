package com.adyen.checkout.googlepay

import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.googlepay.model.GooglePayParamUtils
import com.adyen.checkout.googlepay.util.AllowedCardNetworks
import org.junit.Assert.assertEquals
import org.junit.Test

class GooglePayParamUtilsTest {

    @Test
    fun testMasterCardTxVariantToGooglePayCodeMapping() {
        val mc = "mc"
        assertEquals(AllowedCardNetworks.MASTERCARD, GooglePayParamUtils.mapTxVariantToGooglePayCode(mc))
    }

    @Test
    fun testOtherTxVariantToGooglePayCodeMapping() {
        val amex = "amex"
        assertEquals(AllowedCardNetworks.AMEX, GooglePayParamUtils.mapTxVariantToGooglePayCode(amex))
    }

    @Test(expected = CheckoutException::class)
    fun testUnsupportedTxVariantToGooglePayCodeMapping() {
        val maestro = "maestro"
        GooglePayParamUtils.mapTxVariantToGooglePayCode(maestro)
    }
}
