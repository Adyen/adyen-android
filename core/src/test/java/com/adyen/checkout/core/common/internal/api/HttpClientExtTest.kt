/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.core.common.internal.api

import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.components.data.Address
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class HttpClientExtTest(
    @param:Mock private val httpClient: HttpClient,
) {

    @Test
    fun `when the response is a json array then every item is deserialized`() = runTest {
        // GIVEN
        val body = """[{"city":"Amsterdam","country":"NL"},{"city":"Berlin","country":"DE"}]"""
        whenever(httpClient.get(eq(PATH), any(), any())).thenReturn(AdyenApiResponse(PATH, 200, emptyMap(), body))

        // WHEN
        val result = httpClient.getList(PATH, Address.SERIALIZER)

        // THEN
        val expected = listOf(
            createAddress(city = "Amsterdam", country = "NL"),
            createAddress(city = "Berlin", country = "DE"),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when the response is empty then the list is empty`() = runTest {
        // GIVEN
        whenever(httpClient.get(eq(PATH), any(), any())).thenReturn(AdyenApiResponse(PATH, 200, emptyMap(), "[]"))

        // WHEN
        val result = httpClient.getList(PATH, Address.SERIALIZER)

        // THEN
        assertTrue(result.isEmpty())
    }

    private fun createAddress(city: String, country: String) = Address(
        city = city,
        country = country,
        houseNumberOrName = null,
        postalCode = null,
        stateOrProvince = null,
        street = null,
    )

    companion object {
        private const val PATH = "datasets/test.json"
    }
}
