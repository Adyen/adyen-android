/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class MBWayDelegateStateFactoryTest(
    @Mock private val componentParams: ButtonComponentParams,
) {

    private lateinit var factory: MBWayDelegateStateFactory

    @BeforeEach
    fun setup() {
        factory = MBWayDelegateStateFactory(componentParams)
    }

    @Test
    fun `when createDefaultDelegateState is called, then state should contain supported and default countries`() {
        val supportedCountries = listOf(
            CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
            CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
        )
        val locale = Locale("en", "US")
        whenever(componentParams.shopperLocale) doReturn locale

        val delegateState = factory.createDefaultDelegateState()

        assertEquals(supportedCountries, delegateState.countries)
        // Assert that the selected country is Portugal (since it's the first in the list)
        assertEquals("PT", delegateState.countryCodeFieldState.value.isoCode)
    }

    @Test
    fun `when getFieldIds is called, then it should return the list of MBWayFieldIds`() {
        val fieldIds = factory.getFieldIds()

        assertEquals(MBWayFieldId.entries, fieldIds)
    }
}
