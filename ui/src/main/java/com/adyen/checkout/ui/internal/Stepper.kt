/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/7/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.test.R

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun Stepper(
    steps: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = AdyenCheckoutTheme.colors.container,
                shape = RoundedCornerShape(AdyenCheckoutTheme.elements.cornerRadius.dp),
            )
            .padding(12.dp),
    ) {
        steps.forEachIndexed { index, step ->
            Step(
                step = step,
                showConnector = index < steps.lastIndex,
            )
        }
    }
}

@Composable
private fun Step(
    step: String,
    showConnector: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Image(painterResource(R.drawable.ic_rounded_square), null)
        Body(step)
    }

    if (showConnector) {
        // TODO - correctly center this under the icon
        VerticalDivider(
            Modifier
                .height(12.dp)
                .padding(start = 8.dp),
            color = AdyenCheckoutTheme.colors.outline,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StepperPreview() {
    val steps = listOf("Step 1", "Step 2", "Step 3")
    Stepper(steps, Modifier.padding(16.dp))
}
