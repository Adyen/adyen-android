/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.TestFormElementId.NUMBER
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FormStateTest {

    @Nested
    inner class IsFormValidTest {

        @Test
        fun `when every element is valid, then the form is valid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), valid(HOLDER_NAME)))

            // WHEN
            val isFormValid = form.isFormValid

            // THEN
            assertTrue(isFormValid)
        }

        @Test
        fun `when an element is invalid, then the form is invalid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(HOLDER_NAME)))

            // WHEN
            val isFormValid = form.isFormValid

            // THEN
            assertFalse(isFormValid)
        }

        @Test
        fun `when the form has no elements, then it is valid`() {
            // GIVEN
            val form = FormState<TestFormElementId>(elements = emptyList())

            // WHEN
            val isFormValid = form.isFormValid

            // THEN
            assertTrue(isFormValid)
        }
    }

    @Nested
    inner class IsElementVisibleAndValidTest {

        @Test
        fun `when an element is on the form and valid, then it is reported as valid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(HOLDER_NAME)))

            // WHEN
            val isVisibleAndValid = form.isElementVisibleAndValid(NUMBER)

            // THEN
            assertTrue(isVisibleAndValid)
        }

        @Test
        fun `when an element is on the form and invalid, then it is not reported as valid`() {
            // GIVEN
            val form = FormState(elements = listOf(valid(NUMBER), invalid(HOLDER_NAME)))

            // WHEN
            val isVisibleAndValid = form.isElementVisibleAndValid(HOLDER_NAME)

            // THEN
            assertFalse(isVisibleAndValid)
        }

        @Test
        fun `when an element is not on the form, then it is not reported as valid`() {
            // GIVEN
            val form = formOf(NUMBER)

            // WHEN
            val isVisibleAndValid = form.isElementVisibleAndValid(HOLDER_NAME)

            // THEN
            assertFalse(isVisibleAndValid)
        }
    }
}
