/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import androidx.annotation.StringRes

internal sealed class EditSettingsData(
    open val identifier: SettingsIdentifier,
) {
    class Text(
        override val identifier: SettingsIdentifier,
        @StringRes val titleResId: Int,
        val text: String,
        val inputType: InputType = InputType.STRING,
    ) : EditSettingsData(identifier){
        enum class InputType{
            STRING, INTEGER
        }
    }

    class SingleSelectList(
        override val identifier: SettingsIdentifier,
        @StringRes val titleResId: Int,
        val items: List<Item>
    ) : EditSettingsData(identifier) {
        data class Item(
            @StringRes val textResId: Int,
            val value: String,
        )
    }
}
