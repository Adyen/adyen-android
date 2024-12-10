/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2023.
 */

package com.adyen.checkout.example.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun ResultContent(
    resultState: ResultState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(ExampleTheme.dimensions.grid_2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = resultState.drawable),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(100.dp),
        )
        Spacer(modifier = Modifier.height(ExampleTheme.dimensions.grid_2))
        Text(text = resultState.text, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentPreview() {
    ResultContent(ResultState.SUCCESS)
}
