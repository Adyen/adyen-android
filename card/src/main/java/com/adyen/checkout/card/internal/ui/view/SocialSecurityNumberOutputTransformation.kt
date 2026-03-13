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
import androidx.compose.foundation.text.input.insert
import com.adyen.checkout.card.internal.ui.view.SocialSecurityNumberProperties.CNPJ_SEPARATORS
import com.adyen.checkout.card.internal.ui.view.SocialSecurityNumberProperties.CPF_LENGTH_LIMIT
import com.adyen.checkout.card.internal.ui.view.SocialSecurityNumberProperties.CPF_SEPARATORS

internal class SocialSecurityNumberOutputTransformation : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        val separators = if (length <= CPF_LENGTH_LIMIT) CPF_SEPARATORS else CNPJ_SEPARATORS

        separators.forEach { separator ->
            if (length > separator.index) insert(separator.index, separator.character.toString())
        }
    }
}
