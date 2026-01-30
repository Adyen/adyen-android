/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 29/1/2026.
 */

package com.adyen.checkout.sessions.core

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SessionSetupConfigurationTest {

    @Test
    fun `when serializing deserialized configuration, then all fields should be serialized`() {
        val json = """
            {
              "showInstallmentAmount": true,
              "installmentOptions": {
                "card": {
                  "plans": ["with_interest"],
                  "values": [1, 2, 3, 6],
                  "preselectedValue": 2
                },
                "visa": {
                  "plans": ["regular", "revolving"],
                  "values": [1, 2, 3, 4, 5, 12]
                }
              },
              "showRemovePaymentMethodButton": true
            }
        """.trimIndent()
        val deserialized: SessionSetupConfiguration = SessionSetupConfiguration.SERIALIZER.deserialize(JSONObject(json))

        val serialized = SessionSetupConfiguration.SERIALIZER.serialize(deserialized)
        println("*************")
        println(serialized["installmentOptions"]) // The value is {"card":null,"visa":null}
        println("*************")

        assertEquals(true, serialized.optBoolean("showInstallmentAmount"))
        assertEquals(true, serialized.optBoolean("showRemovePaymentMethodButton"))
        assertTrue(serialized.has("installmentOptions"))
        val installmentOptions = serialized.optJSONObject("installmentOptions")
        assertTrue("installmentOptions should not be null", installmentOptions != null)
        val cardOptions = installmentOptions!!.optJSONObject("card")
        assertTrue("card options should not be null", cardOptions != null)
        assertEquals(
            listOf("with_interest"),
            cardOptions!!.optJSONArray("plans")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            },
        )
        assertEquals(
            listOf(1, 2, 3, 6),
            cardOptions.optJSONArray("values")?.let { arr ->
                (0 until arr.length()).map { arr.getInt(it) }
            },
        )
        assertTrue("card preselectedValue should be null", cardOptions.isNull("preselectedValue"))
        val visaOptions = installmentOptions.optJSONObject("visa")
        assertTrue("visa options should not be null", visaOptions != null)
        assertEquals(
            listOf("regular", "revolving"),
            visaOptions!!.optJSONArray("plans")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            },
        )
        assertEquals(
            listOf(1, 2, 3, 4, 5, 12),
            visaOptions.optJSONArray("values")?.let { arr ->
                (0 until arr.length()).map { arr.getInt(it) }
            },
        )
        assertTrue("visa preselectedValue should be null", visaOptions.isNull("preselectedValue"))
    }

    @Test
    fun `when deserializing with all fields, then all fields should be deserialized correctly`() {
        val json = """
            {
              "amount": {
                "currency": "EUR",
                "value": 11295
              },
              "billingAddress": {
                "city": "Ankeborg",
                "country": "SE",
                "houseNumberOrName": "1",
                "postalCode": "1234",
                "street": "Stargatan"
              },
              "channel": "Android",
              "countryCode": "NL",
              "dateOfBirth": "1996-09-04T02:00:00+02:00",
              "deliveryAddress": {
                "city": "Ankeborg",
                "country": "SE",
                "houseNumberOrName": "1",
                "postalCode": "1234",
                "street": "Stargatan"
              },
              "expiresAt": "2026-01-29T15:18:56+01:00",
              "id": "CS324462C76289221F6E9627C",
              "installmentOptions": {
                "card": {
                  "plans": ["with_interest"],
                  "values": [1, 2, 3, 6]
                },
                "visa": {
                  "plans": ["regular", "revolving"],
                  "values": [1, 2, 3, 4, 5, 12]
                },
                "mc": {
                  "plans": ["regular", "revolving"],
                  "values": [1, 2, 3, 4, 5, 12]
                }
              },
              "lineItems": [
                {
                  "amountExcludingTax": 331,
                  "amountIncludingTax": 400,
                  "description": "Shoes",
                  "id": "Item #1",
                  "imageUrl": "URL_TO_PICTURE_OF_PURCHASED_ITEM",
                  "productUrl": "URL_TO_PURCHASED_ITEM",
                  "quantity": 1,
                  "taxAmount": 69,
                  "taxPercentage": 2100
                }
              ],
              "merchantAccount": "FlutterTEST",
              "recurringProcessingModel": "CardOnFile",
              "reference": "flutter-session-test_1769692736584",
              "returnUrl": "adyencheckout://com.adyen.adyen_checkout_example",
              "shopperInteraction": "Ecommerce",
              "shopperLocale": "en-US",
              "shopperReference": "Test reference",
              "socialSecurityNumber": "0108",
              "storePaymentMethodMode": "disabled",
              "telephoneNumber": "+8613012345678",
              "mode": "embedded",
              "sessionData": "Ab02b4c0!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"
            }
        """.trimIndent()

        val actual = SessionSetupConfiguration.SERIALIZER.deserialize(JSONObject(json))

        assertNull(actual.enableStoreDetails)
        assertEquals(false, actual.showInstallmentAmount)
        assertEquals(3, actual.installmentOptions?.size)
        assertEquals(listOf("with_interest"), actual.installmentOptions?.get("card")?.plans)
        assertEquals(listOf(1, 2, 3, 6), actual.installmentOptions?.get("card")?.values)
        assertNull(actual.installmentOptions?.get("card")?.preselectedValue)
        assertEquals(listOf("regular", "revolving"), actual.installmentOptions?.get("visa")?.plans)
        assertEquals(listOf(1, 2, 3, 4, 5, 12), actual.installmentOptions?.get("visa")?.values)
        assertNull(actual.installmentOptions?.get("visa")?.preselectedValue)
        assertEquals(listOf("regular", "revolving"), actual.installmentOptions?.get("mc")?.plans)
        assertEquals(listOf(1, 2, 3, 4, 5, 12), actual.installmentOptions?.get("mc")?.values)
        assertNull(actual.installmentOptions?.get("mc")?.preselectedValue)
        assertNull(actual.showRemovePaymentMethodButton)
    }
}
