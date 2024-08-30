/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    allowNumbersOnly: Boolean = false,
    placeholder: String? = null,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }
    val focusRequester = remember { FocusRequester() }

    GenericDialog(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = {
            val textFieldModifier = Modifier.focusRequester(focusRequester)

            val onValueChange: (TextFieldValue) -> Unit = onValueChange@{
                if (allowNumbersOnly && !it.text.isDigitsOnly()) {
                    return@onValueChange
                }
                textFieldValue = it
            }

            val keyboardOptions = if (allowNumbersOnly) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            }

            val placeholderBlock: @Composable (() -> Unit)? = placeholder?.let {
                { Text(text = placeholder) }
            }

            OutlinedTextField(
                modifier = textFieldModifier,
                value = textFieldValue,
                onValueChange = onValueChange,
                keyboardOptions = keyboardOptions,
                placeholder = placeholderBlock,
            )

            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose {}
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(textFieldValue.text) }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
fun EditTextDialogPreview() {
    ExampleTheme {
        TextFieldDialog(
            title = "Title",
            content = "Content",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

