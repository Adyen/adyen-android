/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/11/2026.
 */

package com.adyen.checkout.googlepay

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MerchantInfoTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val merchantInfo = MerchantInfo(
            merchantName = "Merchant",
            softwareInfo = SoftwareInfo(id = "software_id", version = "1.0.0"),
        )

        val actual = MerchantInfo.SERIALIZER.serialize(merchantInfo)

        val expected = JSONObject()
            .put("merchantName", "Merchant")
            .put(
                "softwareInfo",
                JSONObject()
                    .put("id", "software_id")
                    .put("version", "1.0.0"),
            )

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing a merchantId, then it is ignored`() {
        val json = JSONObject()
            .put("merchantName", "Merchant")
            .put("merchantId", "merchant_id")

        val actual = MerchantInfo.SERIALIZER.deserialize(json)

        assertEquals(MerchantInfo(merchantName = "Merchant"), actual)
    }
}
