/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/9/2024.
 */

package com.adyen.checkout.example.ui.settings.model

internal sealed class EditSettingData(
    open val identifier: SettingsIdentifier,
) {
    data class Text(
        override val identifier: SettingsIdentifier,
        val value: String,
    ) : EditSettingData(identifier)

    data class Switch(
        override val identifier: SettingsIdentifier,
        val value: Boolean,
    ) : EditSettingData(identifier)

    data class ListItem(
        override val identifier: SettingsIdentifier,
        val value: Enum<*>,
    ) : EditSettingData(identifier)
}
