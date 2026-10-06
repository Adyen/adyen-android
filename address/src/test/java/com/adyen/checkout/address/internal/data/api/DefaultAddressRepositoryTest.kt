/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.api

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.error.internal.HttpError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultAddressRepositoryTest(
    @param:Mock private val addressService: AddressService,
) {

    private lateinit var repository: DefaultAddressRepository

    @BeforeEach
    fun beforeEach() {
        repository = DefaultAddressRepository(addressService, LOCALE)
    }

    @Test
    fun `when the countries are requested twice then they are only fetched once`() = runTest {
        // GIVEN
        whenever(addressService.getCountries(LOCALE)) doReturn COUNTRIES

        // WHEN
        repository.getCountries()
        val result = repository.getCountries()

        // THEN
        assertEquals(COUNTRIES, result.getOrNull())
        verify(addressService, times(1)).getCountries(LOCALE)
    }

    @Test
    fun `when the states of two countries are requested then each country is fetched`() = runTest {
        // GIVEN
        whenever(addressService.getStates(LOCALE, "US")) doReturn US_STATES
        whenever(addressService.getStates(LOCALE, "CA")) doReturn CA_PROVINCES

        // WHEN
        val usResult = repository.getStates("US")
        val caResult = repository.getStates("CA")

        // THEN
        assertEquals(US_STATES, usResult.getOrNull())
        assertEquals(CA_PROVINCES, caResult.getOrNull())
        verify(addressService).getStates(LOCALE, "US")
        verify(addressService).getStates(LOCALE, "CA")
    }

    @Test
    fun `when the states dataset does not exist then the result is an empty list`() = runTest {
        // GIVEN
        whenever(addressService.getStates(LOCALE, "NL")) doThrow HttpError(404, "Not found", null)

        // WHEN
        val result = repository.getStates("NL")

        // THEN
        assertEquals(emptyList<AddressItem>(), result.getOrNull())
    }

    @Test
    fun `when a missing states dataset is requested again then it is not fetched again`() = runTest {
        // GIVEN
        whenever(addressService.getStates(LOCALE, "NL")) doThrow HttpError(404, "Not found", null)

        // WHEN
        repository.getStates("NL")
        val result = repository.getStates("NL")

        // THEN
        assertEquals(emptyList<AddressItem>(), result.getOrNull())
        verify(addressService, times(1)).getStates(LOCALE, "NL")
    }

    @ParameterizedTest
    @MethodSource("statesFailureSource")
    fun `when the states request fails for another reason then the result is a failure`(error: Throwable) = runTest {
        // GIVEN
        whenever(addressService.getStates(LOCALE, "US")).thenAnswer { throw error }

        // WHEN
        val result = repository.getStates("US")

        // THEN
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `when a failed request is retried then it is fetched again`() = runTest {
        // GIVEN
        whenever(addressService.getStates(LOCALE, "US"))
            .thenAnswer { throw IOException("No network") }
            .thenReturn(US_STATES)

        // WHEN
        repository.getStates("US")
        val result = repository.getStates("US")

        // THEN
        assertEquals(US_STATES, result.getOrNull())
        verify(addressService, times(2)).getStates(LOCALE, "US")
    }

    @Test
    fun `when the countries request fails then the result is a failure`() = runTest {
        // GIVEN
        val error = IOException("No network")
        whenever(addressService.getCountries(LOCALE)).thenAnswer { throw error }

        // WHEN
        val result = repository.getCountries()

        // THEN
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `when the country code is blank then no request is made`() = runTest {
        // WHEN
        val result = repository.getStates(" ")

        // THEN
        assertTrue(result.getOrThrow().isEmpty())
        verifyNoInteractions(addressService)
    }

    companion object {
        private val LOCALE = Locale.US

        private val COUNTRIES = listOf(
            AddressItem(id = "NL", name = "Netherlands"),
            AddressItem(id = "US", name = "United States"),
        )
        private val US_STATES = listOf(
            AddressItem(id = "CA", name = "California"),
            AddressItem(id = "NY", name = "New York"),
        )
        private val CA_PROVINCES = listOf(
            AddressItem(id = "ON", name = "Ontario"),
        )

        @JvmStatic
        fun statesFailureSource() = listOf(
            arguments(IOException("No network")),
            arguments(HttpError(500, "Internal server error", null)),
        )
    }
}
