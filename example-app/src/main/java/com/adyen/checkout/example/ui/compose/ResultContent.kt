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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.example.ui.theme.LocalCustomColorScheme

@Composable
internal fun ResultContent(
    resultState: ResultState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val tint = when (resultState) {
            ResultState.SUCCESS -> LocalCustomColorScheme.current.success
            ResultState.PENDING -> LocalCustomColorScheme.current.warning
            ResultState.FAILURE -> MaterialTheme.colorScheme.error
        }
        Icon(
            painter = painterResource(id = resultState.drawable),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(100.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = resultState.text, style = MaterialTheme.typography.displaySmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentPreview() {
    ResultContent(ResultState.SUCCESS)
}
