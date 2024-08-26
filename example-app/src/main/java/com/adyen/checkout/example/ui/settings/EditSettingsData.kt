/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings

internal sealed interface EditSettingsData {
    // TODO: add validation for numbers, emails, etc
    class Text(val text: String) : EditSettingsData
    class SingleSelectList(val items: List<Item>) : EditSettingsData {
        data class Item(
            val text: String,
            val value: String,
        )
    }

    class Switch(val selected: Boolean) : EditSettingsData
}
