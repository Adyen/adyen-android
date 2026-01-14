/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.BodyEmphasized
import com.adyen.checkout.ui.internal.text.SubHeadline
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

/**
 * A list item component that displays a title, an optional subtitle, an optional leading icon and optional trailing
 * content.
 *
 * @param title The text to display as the main title of the item.
 * @param onClick The callback to be invoked when the item is clicked.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param subtitle The optional text to display below the title.
 * @param leadingIcon The optional composable to display at the start of the item. **This should only be used for
 * icons!**
 * @param trailingContent The optional composable to display at the end of the item.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Medium),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp))
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(Dimensions.Spacing.Medium),
    ) {
        leadingIcon?.let { icon ->
            icon()
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            BodyEmphasized(title)

            subtitle?.let {
                SubHeadline(
                    text = it,
                    color = CheckoutThemeProvider.colors.textSecondary,
                )
            }
        }

        trailingContent?.let { content ->
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    ListItem(title = "Title", onClick = {}, subtitle = "Subtitle")
}

@Preview(showBackground = true)
@Composable
private fun ListItemNoSubtitlePreview() {
    ListItem(title = "Title", onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithLeadingIconNoSubtitlePreview() {
    ListItem(
        title = "Title",
        onClick = {},
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
private fun ListItemWithLeadingIconPreview() {
    ListItem(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
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
private fun ListItemWithTrailingIconPreview() {
    ListItem(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        trailingContent = {
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
private fun ListItemWithTrailingActionPreview() {
    ListItem(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        trailingContent = {
            Body("Action")
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithIconsPreview() {
    ListItem(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(
                    R.drawable.ic_placeholder_image,
                ),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
            )
        },
        trailingContent = {
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

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ListItemListPreview() {
    Column {
        repeat(5) {
            ListItem(title = "Title", onClick = {}, subtitle = "Subtitle")
        }
    }
}
