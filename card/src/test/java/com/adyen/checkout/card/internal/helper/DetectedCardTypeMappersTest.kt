/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/5/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.BinLookupBrand
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.state.NetworkBinLookupState
import com.adyen.checkout.core.common.CardBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DetectedCardTypeMappersTest {

    @Test
    fun `when single supported brand then correct BinLookupData is returned`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(createDetectedCardType(brand = "visa", isSupported = true)),
            issuingCountryCode = "NL",
        )

        val result = detectedCardTypeList.toBinLookupData()

        val expected = BinLookupData(
            issuingCountryCode = "NL",
            brands = listOf(
                BinLookupBrand(brand = "visa", supported = true, paymentMethodVariant = "scheme"),
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when mixed support then all brands with correct supported flags are returned`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(
                createDetectedCardType(brand = "visa", isSupported = true),
                createDetectedCardType(brand = "mc", isSupported = false),
            ),
            issuingCountryCode = "US",
        )

        val result = detectedCardTypeList.toBinLookupData()

        val expected = BinLookupData(
            issuingCountryCode = "US",
            brands = listOf(
                BinLookupBrand(brand = "visa", supported = true, paymentMethodVariant = "scheme"),
                BinLookupBrand(brand = "mc", supported = false, paymentMethodVariant = "scheme"),
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when all unsupported then callback fires with supported false entries`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(
                createDetectedCardType(brand = "visa", isSupported = false),
                createDetectedCardType(brand = "mc", isSupported = false),
            ),
            issuingCountryCode = "DE",
        )

        val result = detectedCardTypeList.toBinLookupData()

        assertEquals(2, result.brands.size)
        result.brands.forEach { brand ->
            assertEquals(false, brand.supported)
        }
    }

    @Test
    fun `when empty response then callback fires with empty brands`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = emptyList(),
            issuingCountryCode = null,
        )

        val result = detectedCardTypeList.toBinLookupData()

        val expected = BinLookupData(
            issuingCountryCode = null,
            brands = emptyList(),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when issuingCountryCode is present then it is propagated correctly`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(createDetectedCardType(brand = "visa", isSupported = true)),
            issuingCountryCode = "FR",
        )

        val result = detectedCardTypeList.toBinLookupData()

        assertEquals("FR", result.issuingCountryCode)
    }

    @Test
    fun `when issuingCountryCode is null then it is null in result`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(createDetectedCardType(brand = "visa", isSupported = true)),
            issuingCountryCode = null,
        )

        val result = detectedCardTypeList.toBinLookupData()

        assertEquals(null, result.issuingCountryCode)
    }

    @Test
    fun `when paymentMethodVariant is present then it is mapped correctly`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(
                createDetectedCardType(brand = "visa", isSupported = true, paymentMethodVariant = "visadebit"),
            ),
            issuingCountryCode = null,
        )

        val result = detectedCardTypeList.toBinLookupData()

        assertEquals("visadebit", result.brands.first().paymentMethodVariant)
    }

    @Test
    fun `when paymentMethodVariant is null then it is null in result`() {
        val detectedCardTypeList = createNetworkBinLookupState(
            detectedCardTypes = listOf(
                createDetectedCardType(brand = "visa", isSupported = true, paymentMethodVariant = null),
            ),
            issuingCountryCode = null,
        )

        val result = detectedCardTypeList.toBinLookupData()

        assertEquals(null, result.brands.first().paymentMethodVariant)
    }

    private fun createNetworkBinLookupState(
        detectedCardTypes: List<DetectedCardType>,
        issuingCountryCode: String?,
    ) = NetworkBinLookupState(
        detectedCardTypes = detectedCardTypes,
        issuingCountryCode = issuingCountryCode,
    )

    private fun createDetectedCardType(
        brand: String = "visa",
        isSupported: Boolean = true,
        paymentMethodVariant: String? = "scheme",
    ) = DetectedCardType(
        cardBrand = CardBrand(brand),
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = isSupported,
        isShopperSelectionAllowedInDualBranded = false,
        panLength = 16,
        paymentMethodVariant = paymentMethodVariant,
        localizedBrand = null,
    )
}
