/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R

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
            ValuePickerItem(
                item,
                onItemClick,
                Modifier.animateItem(),
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

@Composable
private fun ValuePickerItem(
    item: ValuePickerItem,
    onClick: (ValuePickerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp))
            .let {
                if (item.isSelected) {
                    it.background(CheckoutThemeProvider.colors.container)
                } else {
                    it
                }
            }
            .clickable(
                interactionSource = null,
                indication = ripple(color = CheckoutThemeProvider.colors.text),
            ) {
                onClick(item)
            }
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Column {
            BodyEmphasized(item.title)
            SubHeadline(
                text = item.subtitle,
                color = CheckoutThemeProvider.colors.textSecondary,
            )
        }

        if (item.isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    R.drawable.ic_checkmark,
                ),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
            )
        }
    }
}

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

@Preview(showBackground = true)
@Composable
private fun ValuePickerItemPreview() {
    val item = ValuePickerItem(id = "id", title = "Title", subtitle = "Subtitle", isSelected = true)

    ValuePickerItem(
        item = item,
        onClick = {},
    )
}
