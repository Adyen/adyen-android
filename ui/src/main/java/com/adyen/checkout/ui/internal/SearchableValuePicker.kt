/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SearchableValuePicker(
    items: List<ValuePickerItem>,
    onItemClick: (ValuePickerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        var query by remember { mutableStateOf("") }
        CheckoutTextField(
            label = "Search",
            initialValue = query,
            onValueChange = { query = it },
        )

        Spacer(Modifier.size(Dimensions.Large))

        val filteredItems by remember {
            derivedStateOf {
                items.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.subtitle.contains(query, ignoreCase = true)
                }
            }
        }
        ValuePicker(filteredItems, onItemClick, modifier)
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchableValuePickerPreview() {
    val items = List(5) {
        ValuePickerItem(id = "$it", title = "$it - Title", subtitle = "Subtitle", isSelected = it == 0)
    }

    SearchableValuePicker(
        items = items,
        onItemClick = {},
    )
}
