/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PickerInputComponentStateTest {

    @Test
    fun `when the picker has no error then its form element is valid`() {
        // GIVEN
        val picker = PickerInputComponentState(selected = "US")

        // WHEN
        val element = picker.toFormElement(AddressFormElementId.COUNTRY)

        // THEN
        assertEquals(FormElementState(AddressFormElementId.COUNTRY, isValid = true), element)
    }

    @Test
    fun `when the picker has an error then its form element is invalid`() {
        // GIVEN
        val picker = PickerInputComponentState<String>(
            error = InputError(ERROR_MESSAGE),
        )

        // WHEN
        val element = picker.toFormElement(AddressFormElementId.COUNTRY)

        // THEN
        assertEquals(FormElementState(AddressFormElementId.COUNTRY, isValid = false), element)
    }

    companion object {
        // Any key works: only the presence of an error matters here.
        private val ERROR_MESSAGE = CheckoutLocalizationKey.ADDRESS_STREET_LABEL
    }
}
