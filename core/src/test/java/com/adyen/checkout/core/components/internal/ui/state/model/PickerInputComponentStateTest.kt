/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/11/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PickerInputComponentStateTest {

    @Nested
    inner class VisibleErrorMessageTest {

        @Test
        fun `when there is no error, then there is no visible error message`() {
            // GIVEN
            val picker = PickerInputComponentState<String>(error = null)

            // WHEN
            val visibleErrorMessage = picker.visibleErrorMessage

            // THEN
            assertNull(visibleErrorMessage)
        }

        @Test
        fun `when there is a hidden error, then there is no visible error message`() {
            // GIVEN
            val picker = PickerInputComponentState<String>(error = InputError(ERROR_MESSAGE, isVisible = false))

            // WHEN
            val visibleErrorMessage = picker.visibleErrorMessage

            // THEN
            assertNull(visibleErrorMessage)
        }

        @Test
        fun `when there is a visible error, then its message is the visible error message`() {
            // GIVEN
            val picker = PickerInputComponentState<String>(error = InputError(ERROR_MESSAGE, isVisible = true))

            // WHEN
            val visibleErrorMessage = picker.visibleErrorMessage

            // THEN
            assertEquals(ERROR_MESSAGE, visibleErrorMessage)
        }
    }

    companion object {
        private val ERROR_MESSAGE = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR
    }
}
