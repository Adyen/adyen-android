/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import androidx.annotation.StringRes

internal data class SettingsCategory (
    @StringRes val titleResId: Int,
    val settingsItems: List<SettingsItem>,
)
