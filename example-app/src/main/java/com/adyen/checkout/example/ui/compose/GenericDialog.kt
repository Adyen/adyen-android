/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
fun GenericDialog(
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)?,
    content: (@Composable () -> Unit)?,
    dismissButton: (@Composable () -> Unit)?,
    confirmButton: (@Composable () -> Unit)?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp,
        ) {
            Column {
                Column(Modifier.padding(ExampleTheme.dimensions.grid_2)) {
                    title?.invoke()
                    if (title != null && content != null) {
                        Spacer(Modifier.size(ExampleTheme.dimensions.grid_2))
                    }
                    content?.invoke()
                }
                if (dismissButton != null || confirmButton != null) {
                    Row(
                        Modifier
                            .padding(ExampleTheme.dimensions.grid_1)
                            .fillMaxWidth(),
                        Arrangement.spacedBy(ExampleTheme.dimensions.grid_1, Alignment.End),
                    ) {
                        dismissButton?.invoke()
                        confirmButton?.invoke()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun GenericDialogPreview() {
    ExampleTheme {
        GenericDialog(
            title = { Text("Title") },
            content = { Text("Content") },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Dismiss")
                }
            },
            confirmButton = {
                Button(onClick = { }) {
                    Text("Confirm")
                }
            },
            onDismiss = {},
        )
    }
}
