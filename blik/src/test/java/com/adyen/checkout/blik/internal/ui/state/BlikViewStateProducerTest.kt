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
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BlikViewStateProducerTest {

    private lateinit var producer: BlikViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = BlikViewStateProducer()
    }

    @Test
    fun `when produce is called, then view state is created`() {
        val componentState = BlikComponentState(
            blikCode = TextInputComponentState(
                text = "123456",
                isFocused = true,
                errorMessage = CheckoutLocalizationKey.BLIK_CODE_INVALID,
                showError = true,
            ),
            isLoading = true,
        )

        val actual = producer.produce(componentState)

        val expected = BlikViewState(
            blikCode = TextInputViewState(
                text = "123456",
                isFocused = true,
                supportingText = CheckoutLocalizationKey.BLIK_CODE_INVALID,
                isError = true,
            ),
            isLoading = true,
        )

        assertEquals(expected, actual)
    }
}
