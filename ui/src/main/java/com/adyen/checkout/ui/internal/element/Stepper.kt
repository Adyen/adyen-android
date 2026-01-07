/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Stepper(
    steps: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = CheckoutThemeProvider.colors.container,
                shape = RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp),
            )
            .padding(Dimensions.Spacing.Large),
    ) {
        steps.forEachIndexed { index, step ->
            Step(
                label = step,
                isFirstStep = index == 0,
                isLastStep = index == steps.lastIndex,
            )
        }
    }
}

@Suppress("DestructuringDeclarationWithTooManyEntries")
@Composable
private fun Step(
    label: String,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier,
    ) {
        val (icon, body, upperConnector, lowerConnector) = createRefs()
        Image(
            painter = painterResource(R.drawable.ic_rounded_square),
            contentDescription = null,
            colorFilter = ColorFilter.tint(CheckoutThemeProvider.colors.primary),
            modifier = Modifier.constrainAs(icon) {
                start.linkTo(parent.start)
                top.linkTo(body.top)
                bottom.linkTo(body.bottom)
            },
        )

        // Divide by 2, because top and bottom together should be equal to medium dimension
        val margin = Dimensions.Spacing.Medium.div(2)
        val topMargin = if (isFirstStep) 0.dp else margin
        val bottomMargin = if (isLastStep) 0.dp else margin
        Body(
            text = label,
            modifier = Modifier
                .constrainAs(body) {
                    start.linkTo(icon.end, Dimensions.Spacing.Large)
                    top.linkTo(parent.top, topMargin)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, bottomMargin)
                    width = Dimension.preferredWrapContent
                },
        )

        // Draw line up from icon
        if (!isFirstStep) {
            StepConnector(
                ref = upperConnector,
                iconRef = icon,
                topAnchor = { parent.top },
                bottomAnchor = { icon.top },
            )
        }

        // Draw line down from icon
        if (!isLastStep) {
            StepConnector(
                ref = lowerConnector,
                iconRef = icon,
                topAnchor = { icon.bottom },
                bottomAnchor = { parent.bottom },
            )
        }
    }
}

@Composable
private fun ConstraintLayoutScope.StepConnector(
    ref: ConstrainedLayoutReference,
    iconRef: ConstrainedLayoutReference,
    topAnchor: ConstrainScope.() -> ConstraintLayoutBaseScope.HorizontalAnchor,
    bottomAnchor: ConstrainScope.() -> ConstraintLayoutBaseScope.HorizontalAnchor,
) {
    VerticalDivider(
        color = CheckoutThemeProvider.colors.outline,
        modifier = Modifier.constrainAs(ref) {
            start.linkTo(iconRef.start)
            top.linkTo(topAnchor())
            end.linkTo(iconRef.end)
            bottom.linkTo(bottomAnchor())
            height = Dimension.fillToConstraints
        },
    )
}

@Preview
@Composable
private fun StepperPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Surface(color = CheckoutThemeProvider.colors.background) {
            val steps = listOf(
                "Step 1: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin et lectus in leo varius " +
                    "facilisis sit amet ut ipsum.",
                "Step 2",
                "Step 3",
                "Step 4: Fusce pretium orci ut nibh rutrum mattis. In condimentum augue id justo cursus facilisis.",
                "Step 5",
            )
            Stepper(steps, Modifier.padding(Dimensions.Spacing.Large))
        }
    }
}
