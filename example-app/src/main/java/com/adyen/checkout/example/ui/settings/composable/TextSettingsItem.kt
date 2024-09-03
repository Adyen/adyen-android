/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.settings.model.SettingsItem
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun TextSettingsItem(
    settingsItem: SettingsItem.Text,
    onItemClicked: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked(settingsItem) }
            .padding(ExampleTheme.dimensions.grid_2),
    ) {
        // TODO: create separate style
        Text(
            text = stringResource(id = settingsItem.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringFromUIText(uiText = settingsItem.subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextSettingsItemPreview() {
    ExampleTheme {
        TextSettingsItem(
            settingsItem = SettingsItem.Text(
                identifier = SettingsIdentifier.AMOUNT,
                titleResId = R.string.settings_title_amount,
                subtitle = UIText.String("1337"),
            ),
            onItemClicked = {},
        )
    }
}
