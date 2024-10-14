/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.compose.TextFieldDialog
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.example.ui.settings.model.EditSettingDialogData
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun EditSettingTextFieldDialog(
    settingToEdit: EditSettingDialogData.Text,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val allowNumbersOnly = when (settingToEdit.inputType) {
        EditSettingDialogData.Text.InputType.STRING -> false
        EditSettingDialogData.Text.InputType.INTEGER -> true
    }

    val placeholder = settingToEdit.placeholder?.let { stringFromUIText(uiText = it) }

    TextFieldDialog(
        title = stringResource(id = settingToEdit.titleResId),
        content = settingToEdit.text,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        allowNumbersOnly = allowNumbersOnly,
        placeholder = placeholder,
    )
}

@Preview(showBackground = true)
@Composable
private fun EditSettingTextFieldDialogPreview() {
    ExampleTheme {
        EditSettingTextFieldDialog(
            settingToEdit = EditSettingDialogData.Text(
                identifier = SettingsIdentifier.SHOPPER_LOCALE,
                titleResId = R.string.settings_title_shopper_locale,
                text = "nl-NL",
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
