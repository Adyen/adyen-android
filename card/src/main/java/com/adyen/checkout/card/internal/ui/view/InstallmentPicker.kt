/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.SelectableListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.Dimensions
import java.util.Locale

@Composable
internal fun InstallmentPicker(
    installmentState: InstallmentViewState,
    onInstallmentSelected: (InstallmentModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Title(resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_TITLE))
        Spacer(Modifier.height(Dimensions.Spacing.Small))
        Body(resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_SUBTITLE))
        Spacer(Modifier.height(Dimensions.Spacing.Large))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            installmentState.options.forEach { option ->
                SelectableListItem(
                    title = option.toDisplayText(),
                    isSelected = option == installmentState.selectedOption,
                    onClick = { onInstallmentSelected(option) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InstallmentPickerPreview() {
    val options = listOf(
        InstallmentModel(null, InstallmentOption.ONE_TIME, null, Locale.US, false),
        InstallmentModel(1, InstallmentOption.REVOLVING, null, Locale.US, false),
        InstallmentModel(2, InstallmentOption.REGULAR, null, Locale.US, false),
        InstallmentModel(3, InstallmentOption.REGULAR, null, Locale.US, false),
    )
    InstallmentPicker(
        installmentState = InstallmentViewState(options = options, selectedOption = options.first()),
        onInstallmentSelected = {},
    )
}
