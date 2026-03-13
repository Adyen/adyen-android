/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.internal.feature.socialsecuritynumber.SocialSecurityNumberProperties.ALL_SEPARATORS
import com.adyen.checkout.card.internal.feature.socialsecuritynumber.SocialSecurityNumberProperties.MAX_LENGTH_UNFORMATTED

internal class SocialSecurityNumberInputTransformation : InputTransformation {

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        val text = asCharSequence()

        // only allow digits and separators
        if (text.filterNot { it.isDigit() || ALL_SEPARATORS.contains(it) }.isNotEmpty()) {
            revertAllChanges()
            return
        }

        // validate length without separators
        val cleaned = text.filter { it.isDigit() }
        if (cleaned.length > MAX_LENGTH_UNFORMATTED) {
            revertAllChanges()
            return
        }

        // set the raw value as digits only so that the OutputTransformation can format cleanly
        if (cleaned.toString() != asCharSequence().toString()) {
            replace(0, length, cleaned.toString())
        }
    }
}
