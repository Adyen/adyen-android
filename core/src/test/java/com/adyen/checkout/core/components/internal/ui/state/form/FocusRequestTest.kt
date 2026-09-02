/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequestTest.TestFormElementId.HOLDER_NAME
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequestTest.TestFormElementId.NUMBER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests when a pending focus request stops being pending. Getting this wrong leaves a request outliving the move it
 * asked for, so the shopper's next tap on the same field is treated as programmatic focus.
 */
internal class FocusRequestTest {

    @Test
    fun `when the requested field takes focus, then nothing is left pending`() {
        // GIVEN
        val request = FocusRequest(NUMBER)

        // WHEN
        val remaining = request.remainingAfter(NUMBER, hasFocus = true)

        // THEN
        assertNull(remaining)
    }

    @Test
    fun `when the requested field loses focus, then the request is still pending`() {
        // The move it asked for has not happened, so a focus loss elsewhere must not swallow it.
        val request = FocusRequest(NUMBER)

        assertEquals(request, request.remainingAfter(NUMBER, hasFocus = false))
    }

    @Test
    fun `when another field takes focus, then the request is still pending`() {
        val request = FocusRequest(NUMBER)

        assertEquals(request, request.remainingAfter(HOLDER_NAME, hasFocus = true))
    }

    @Test
    fun `when nothing is pending, then nothing becomes pending`() {
        val request: FocusRequest<TestFormElementId>? = null

        assertNull(request.remainingAfter(NUMBER, hasFocus = true))
    }

    private enum class TestFormElementId(override val isTextInput: Boolean = true) : FormElementId {
        NUMBER,
        HOLDER_NAME,
    }
}
