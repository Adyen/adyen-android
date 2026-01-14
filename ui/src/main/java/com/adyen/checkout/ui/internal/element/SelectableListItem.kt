/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun SelectableListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isSelected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    ListItem(
        title = title,
        onClick = onClick,
        subtitle = subtitle,
        modifier = modifier
            .clip(RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp))
            .background(
                color = if (isSelected) {
                    CheckoutThemeProvider.colors.container
                } else {
                    Color.Transparent
                },
            ),
        leadingIcon = leadingIcon,
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_checkmark),
                    contentDescription = null,
                    tint = CheckoutThemeProvider.colors.text,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemNoSubtitlePreview() {
    SelectableListItem(title = "Title", onClick = {}, isSelected = true)
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemWithLeadingIconNoSubtitlePreview() {
    SelectableListItem(
        title = "Title",
        onClick = {},
        isSelected = true,
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(
                    R.drawable.ic_placeholder_image,
                ),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemUnselectedPreview() {
    SelectableListItem(title = "Title", onClick = {}, subtitle = "Subtitle", isSelected = false)
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemPreview() {
    SelectableListItem(title = "Title", onClick = {}, subtitle = "Subtitle", isSelected = true)
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemWithLeadingIconPreview() {
    SelectableListItem(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        isSelected = true,
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(
                    R.drawable.ic_placeholder_image,
                ),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectableListItemLongTitlePreview() {
    SelectableListItem(
        title = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the " +
            "industry's standard dummy text ever since the 1500s.",
        onClick = {},
        subtitle = "Subtitle",
        isSelected = true,
    )
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun SelectableListItemListPreview() {
    Column {
        repeat(5) {
            SelectableListItem(title = "Title", onClick = {}, subtitle = "Subtitle", isSelected = it == 1)
        }
    }
}
