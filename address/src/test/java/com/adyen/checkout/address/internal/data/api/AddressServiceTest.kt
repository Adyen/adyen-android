/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.api

import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.internal.api.AdyenApiResponse
import com.adyen.checkout.core.common.internal.api.HttpClient
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class AddressServiceTest(
    @param:Mock private val httpClient: HttpClient,
) {

    @BeforeEach
    fun beforeEach() = runTest {
        whenever(httpClient.get(any(), any(), any())).thenReturn(AdyenApiResponse("", 200, emptyMap(), "[]"))
    }

    @Test
    fun `when countries are requested then the dataset path contains the shopper locale language tag`() = runTest {
        // GIVEN
        val service = AddressService(httpClient, UnconfinedTestDispatcher(testScheduler))

        // WHEN
        service.getCountries(Locale.forLanguageTag("pt-BR"))

        // THEN
        verify(httpClient).get(eq("datasets/countries/pt-BR.json"), any(), any())
    }

    @Test
    fun `when states are requested then the dataset path contains the country code and the language tag`() = runTest {
        // GIVEN
        val service = AddressService(httpClient, UnconfinedTestDispatcher(testScheduler))

        // WHEN
        service.getStates(Locale.forLanguageTag("en-US"), "CA")

        // THEN
        verify(httpClient).get(eq("datasets/states/CA/en-US.json"), any(), any())
    }
}
