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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.ProgressBar
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun AwaitComponent(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        ProgressBar()
        Spacer(Modifier.size(Dimensions.Large))
        Body(resolveString(CheckoutLocalizationKey.AWAIT_LOADING))
    }
}

@Preview(showBackground = true)
@Composable
private fun AwaitComponentPreview() {
    AwaitComponent()
}
