/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.settings.model.SettingsItem
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun SwitchSettingsItem(
    settingsItem: SettingsItem.Switch,
    onSwitchSettingChanged: (SettingsIdentifier, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onSwitchSettingChanged(settingsItem.identifier, !settingsItem.checked)
            }
            .padding(ExampleTheme.dimensions.grid_2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = settingsItem.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.weight(1f),
        )

        Switch(
            checked = settingsItem.checked,
            onCheckedChange = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchSettingsItemPreview() {
    ExampleTheme {
        SwitchSettingsItem(
            settingsItem = SettingsItem.Switch(
                identifier = SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
                titleResId = R.string.settings_title_card_installment_show_amount,
                checked = true,
            ),
            onSwitchSettingChanged = { _, _ -> },
        )
    }
}
