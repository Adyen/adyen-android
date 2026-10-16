/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import com.adyen.checkout.address.internal.ui.properties.AddressLineProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressInputTransformationTest {

    @Test
    fun `when a two-line street over the maximum is pasted then it becomes one line cut to the maximum`() {
        // GIVEN
        val state = TextFieldState()
        val transformation = addressInputTransformation(AddressLineProperties.ADDRESS_LINE_MAX_LENGTH)

        // WHEN
        state.edit {
            append("Bürgermeister-Smidt-Straße 12\nApartment 4")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("Bürgermeister-Smidt-Straße 12 ", state.text.toString())
    }

    @Test
    fun `when a two-line street within the maximum is pasted then it is kept whole on one line`() {
        // GIVEN
        val state = TextFieldState()
        val transformation = addressInputTransformation(AddressLineProperties.ADDRESS_LINE_MAX_LENGTH)

        // WHEN
        state.edit {
            append("Main Street 1\nApt 4")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("Main Street 1 Apt 4", state.text.toString())
    }

    @Test
    fun `when a postal code with a trailing line break is pasted then the line break becomes a space`() {
        // GIVEN
        val state = TextFieldState()
        val transformation = addressInputTransformation(maxLength = 10)

        // WHEN
        state.edit {
            append("12345\n")
            with(transformation) { transformInput() }
        }

        // THEN
        assertEquals("12345 ", state.text.toString())
    }
}
