/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties.CNPJ_SEPARATORS
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties.CPF_SEPARATORS
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties.CPF_VALID_LENGTH
import com.adyen.checkout.ui.internal.element.input.SeparatorsTextFieldBufferTransformation

internal class SocialSecurityNumberOutputTransformation : OutputTransformation {

    private val outputTransformation = SeparatorsTextFieldBufferTransformation()

    override fun TextFieldBuffer.transformOutput() {
        val separators = if (length <= CPF_VALID_LENGTH) CPF_SEPARATORS else CNPJ_SEPARATORS

        outputTransformation.transformOutput(this, separators)
    }
}
