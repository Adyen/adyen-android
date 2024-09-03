/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.compose.GenericDialog
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.example.ui.settings.model.EditSettingsData
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun EditSettingListFieldDialog(
    settingToEdit: EditSettingsData.SingleSelectList,
    onConfirm: (EditSettingsData.SingleSelectList.Item) -> Unit,
    onDismiss: () -> Unit,
) {
    GenericDialog(
        title = {
            Text(
                text = stringResource(id = settingToEdit.titleResId),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = {
            LazyColumn {
                items(settingToEdit.items) { item ->
                    Text(
                        modifier = Modifier
                            .clickable { onConfirm(item) }
                            .padding(vertical = ExampleTheme.dimensions.grid_2)
                            .fillMaxWidth(),
                        text = stringFromUIText(uiText = item.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        confirmButton = null,
        dismissButton = null,
        onDismiss = onDismiss,
    )
}

@Preview(showBackground = true)
@Composable
private fun EditSettingListFieldDialogPreview() {
    ExampleTheme {
        EditSettingListFieldDialog(
            settingToEdit = EditSettingsData.SingleSelectList(
                identifier = SettingsIdentifier.THREE_DS_MODE,
                titleResId = R.string.settings_title_threeds_mode,
                items = listOf(
                    EditSettingsData.SingleSelectList.Item(UIText.String("First item"), "first_item"),
                    EditSettingsData.SingleSelectList.Item(UIText.String("Second item"), "second_item"),
                    EditSettingsData.SingleSelectList.Item(UIText.String("Third item"), "third_item"),
                ),
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
