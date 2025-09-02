/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
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
internal class MBWayViewStateFactoryTest(
    @Mock private val componentParams: CommonComponentParams,
) {

    private lateinit var factory: MBWayViewStateFactory

    @BeforeEach
    fun setup() {
        factory = MBWayViewStateFactory(componentParams)
    }

    @Test
    fun `when createDefaultComponentState is called, then conform with expected state`() {
        val countryPT = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351")
        val countryES = CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34")
        val supportedCountries = listOf(countryPT, countryES)
        val locale = Locale("en", "US")
        whenever(componentParams.shopperLocale) doReturn locale

        val actual = factory.createDefaultViewState()

        val expected = MBWayViewState(
            countries = supportedCountries,
            countryCode = countryPT,
            phoneNumber = TextInputState(
                text = "",
                errorMessage = null,
                isFocused = true,
                isEdited = false,
                showError = false,
            ),
            isLoading = false,
        )
        assertEquals(expected, actual)
    }
}
