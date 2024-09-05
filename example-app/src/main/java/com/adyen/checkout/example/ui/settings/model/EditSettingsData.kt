/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.model

import androidx.annotation.StringRes
import com.adyen.checkout.example.ui.compose.UIText

internal sealed class EditSettingsData(
    open val identifier: SettingsIdentifier,
) {
    data class Text(
        override val identifier: SettingsIdentifier,
        @StringRes val titleResId: Int,
        val text: String,
        val inputType: InputType = InputType.STRING,
        val placeholder: UIText? = null,
    ) : EditSettingsData(identifier) {
        enum class InputType {
            STRING,
            INTEGER,
        }
    }

    data class SingleSelectList(
        override val identifier: SettingsIdentifier,
        @StringRes val titleResId: Int,
        val items: List<Item>
    ) : EditSettingsData(identifier) {
        data class Item(
            val text: UIText,
            val value: String,
        )
    }
}
