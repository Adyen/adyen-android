/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Stable
import com.adyen.checkout.core.common.internal.properties.CardNumberProperties.CARD_NUMBER_AMEX_SEPARATORS
import com.adyen.checkout.core.common.internal.properties.CardNumberProperties.CARD_NUMBER_DEFAULT_SEPARATORS
import com.adyen.checkout.ui.internal.element.input.SeparatorsTextFieldBufferTransformation

@Stable
internal class CardNumberOutputTransformation(
    val isAmex: Boolean,
) : OutputTransformation {

    private val outputTransformation = SeparatorsTextFieldBufferTransformation()
    private val separators = if (isAmex) CARD_NUMBER_AMEX_SEPARATORS else CARD_NUMBER_DEFAULT_SEPARATORS

    override fun TextFieldBuffer.transformOutput() {
        outputTransformation.transformOutput(this, separators)
    }
}
