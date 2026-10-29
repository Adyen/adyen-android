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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.BodyEmphasized
import com.adyen.checkout.ui.internal.text.SubHeadline
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A list item component that displays a title, an optional subtitle, an optional leading icon and optional trailing
 * content.
 *
 * @param title The text to display as the main title of the item.
 * @param onClick The callback to be invoked when the item is clicked.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param enabled Whether the item can be clicked. A disabled item ignores clicks and is not focusable.
 * @param subtitle The optional text to display below the title.
 * @param subtitleColor The color of the subtitle while the item is enabled. Defaults to the secondary text color.
 * @param selected Whether the item is selected. A selected item is highlighted and, if no [trailingContent] is
 * provided, shows a checkmark.
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
    enabled: Boolean = true,
    subtitle: String? = null,
    subtitleColor: Color? = null,
    selected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val cornerRadius = CheckoutThemeProvider.attributes.cornerRadius.dp
    val shapes = remember(cornerRadius) {
        val shape = RoundedCornerShape(cornerRadius)
        ListItemShapes(
            shape = shape,
            selectedShape = shape,
            pressedShape = shape,
            focusedShape = shape,
            hoveredShape = shape,
            draggedShape = shape,
        )
    }

    val checkoutColors = CheckoutThemeProvider.colors

    ListItem(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        content = {
            BodyEmphasized(
                text = title,
                color = if (enabled) checkoutColors.text else checkoutColors.textOnDisabled,
            )
        },
        supportingContent = subtitle?.let {
            {
                SubHeadline(
                    text = it,
                    color = when {
                        !enabled -> checkoutColors.textOnDisabled
                        else -> subtitleColor ?: checkoutColors.textSecondary
                    },
                )
            }
        },
        leadingContent = leadingIcon,
        trailingContent = trailingContent ?: if (selected) {
            { SelectedCheckmark() }
        } else {
            null
        },
        shapes = shapes,
        contentPadding = DefaultPaddingValues,
        colors = listItemColors(),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    )
}

@Composable
private fun listItemColors(): ListItemColors {
    val checkoutColors = CheckoutThemeProvider.colors
    return ListItemDefaults.colors(
        containerColor = checkoutColors.background,
        contentColor = checkoutColors.text,
        leadingContentColor = checkoutColors.text,
        trailingContentColor = checkoutColors.text,
        selectedContainerColor = checkoutColors.container,
        selectedContentColor = checkoutColors.text,
        selectedLeadingContentColor = checkoutColors.text,
        selectedTrailingContentColor = checkoutColors.text,
        disabledContainerColor = checkoutColors.background,
        disabledContentColor = checkoutColors.textOnDisabled,
        disabledLeadingContentColor = checkoutColors.textOnDisabled,
        disabledTrailingContentColor = checkoutColors.textOnDisabled,
        disabledSupportingContentColor = checkoutColors.textOnDisabled,
    )
}

@Composable
private fun SelectedCheckmark() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_checkmark),
        contentDescription = null,
        tint = CheckoutThemeProvider.colors.text,
        modifier = Modifier.size(Dimensions.IconSize.small),
    )
}

private val DefaultPaddingValues = PaddingValues(Dimensions.Spacing.Medium)

@Preview(showBackground = true)
@Composable
private fun ListItemPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ListItem(title = "Title", onClick = {}, subtitle = "Subtitle")
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemDisabledPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ListItem(title = "Title", onClick = {}, subtitle = "Subtitle", enabled = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemNoSubtitlePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ListItem(title = "Title", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithLeadingIconNoSubtitlePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
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
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithLeadingIconPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
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
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithTrailingIconPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
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
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithTrailingActionPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ListItem(
            title = "Title",
            onClick = {},
            subtitle = "Subtitle",
            trailingContent = {
                Body("Action")
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemWithIconsPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
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
}

@Preview(showBackground = true)
@Composable
private fun ListItemSelectedPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ListItem(title = "Title", onClick = {}, subtitle = "Subtitle", selected = true)
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ListItemListPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        Column {
            repeat(5) {
                ListItem(title = "Title", onClick = {}, subtitle = "Subtitle", selected = it == 1)
            }
        }
    }
}
