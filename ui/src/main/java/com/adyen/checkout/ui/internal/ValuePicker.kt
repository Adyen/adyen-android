/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun ValuePicker(
    items: List<ValuePickerItem>,
    onItemClick: (ValuePickerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
        modifier = modifier,
    ) {
        items(
            items = items,
            key = { it.id },
        ) { item ->
            SelectableListItem(
                title = item.title,
                subtitle = item.subtitle,
                isSelected = item.isSelected,
                onClick = { onItemClick(item) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ValuePickerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isSelected: Boolean,
)

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ValuePickerPreview() {
    val items = List(5) {
        ValuePickerItem(id = "$it", title = "Title", subtitle = "Subtitle", isSelected = it == 0)
    }

    ValuePicker(
        items = items,
        onItemClick = {},
    )
}
