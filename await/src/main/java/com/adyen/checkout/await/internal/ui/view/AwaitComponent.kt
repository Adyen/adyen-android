/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/7/2025.
 */

package com.adyen.checkout.await.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.ui.internal.Body
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.ProgressBar

@Composable
internal fun AwaitComponent(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Medium),
    ) {
        ProgressBar()
        Spacer(Modifier.size(Dimensions.Large))
        // TODO - Localisation, add the correct text
        Body("Awaiting confirmation...")
    }
}

@Preview(showBackground = true)
@Composable
private fun AwaitComponentPreview() {
    AwaitComponent()
}
