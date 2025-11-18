/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ListOption(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Medium),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp))
            .background(
                color = if (isSelected) {
                    CheckoutThemeProvider.colors.container
                } else {
                    Color.Transparent
                },
            )
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(Dimensions.Medium),
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

        trailingIcon?.let { icon ->
            icon()
        }

        if (isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_checkmark),
                contentDescription = null,
                tint = CheckoutThemeProvider.colors.text,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListOptionPreview() {
    ListOption(title = "Title", onClick = {}, subtitle = "Subtitle")
}

@Preview(showBackground = true)
@Composable
private fun ListOptionNoSubtitlePreview() {
    ListOption(title = "Title", onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun ListOptionWithLeadingIconNoSubtitlePreview() {
    ListOption(
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
private fun ListOptionWithLeadingIconPreview() {
    ListOption(
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
private fun ListOptionWithTrailingIconPreview() {
    ListOption(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        trailingIcon = {
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
private fun ListOptionWithIconsPreview() {
    ListOption(
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
        trailingIcon = {
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
private fun SelectedListOptionPreview() {
    ListOption(title = "Title", onClick = {}, subtitle = "Subtitle", isSelected = true)
}

@Preview(showBackground = true)
@Composable
private fun SelectedListOptionLongTitlePreview() {
    ListOption(
        title = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the " +
            "industry's standard dummy text ever since the 1500s.",
        onClick = {},
        subtitle = "Subtitle",
        isSelected = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectedListOptionWithTrailingIconPreview() {
    ListOption(
        title = "Title",
        onClick = {},
        subtitle = "Subtitle",
        isSelected = true,
        trailingIcon = {
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
private fun ListOptionListPreview() {
    Column {
        repeat(5) {
            ListOption(title = "Title", onClick = {}, subtitle = "Subtitle", isSelected = it == 1)
        }
    }
}
