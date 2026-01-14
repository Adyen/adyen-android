/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BlikComponentStateFactoryTest {

    private lateinit var factory: BlikComponentStateFactory

    @BeforeEach
    fun beforeEach() {
        factory = BlikComponentStateFactory()
    }

    @Test
    fun `when creating initial state, then conform with expected state`() {
        val actual = factory.createInitialState()

        val expected = BlikComponentState(
            blikCode = TextInputComponentState(
                text = "",
                description = CheckoutLocalizationKey.BLIK_CODE_HINT,
                errorMessage = null,
                isFocused = true,
                showError = false,
            ),
            isLoading = false,
        )
        assertEquals(expected, actual)
    }
}
