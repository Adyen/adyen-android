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
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.card.internal.ui.model.toDisplayText
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.SelectableListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions
import java.util.Locale

@Composable
internal fun InstallmentPicker(
    installmentOptions: List<InstallmentModel>,
    selectedInstallmentOption: InstallmentModel?,
    onInstallmentSelected: (InstallmentModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Body(resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_SUBTITLE))
        Spacer(Modifier.height(Dimensions.Spacing.Large))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.ExtraSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            installmentOptions.forEach { option ->
                SelectableListItem(
                    title = option.toDisplayText(),
                    isSelected = option == selectedInstallmentOption,
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
        InstallmentModel(null, InstallmentPlan.NONE, null, Locale.US, false),
        InstallmentModel(1, InstallmentPlan.REVOLVING, null, Locale.US, false),
        InstallmentModel(2, InstallmentPlan.REGULAR, null, Locale.US, false),
        InstallmentModel(3, InstallmentPlan.REGULAR, null, Locale.US, false),
    )
    InstallmentPicker(
        installmentOptions = options,
        selectedInstallmentOption =  InstallmentModel(2, InstallmentPlan.REGULAR, null, Locale.US, false),
        onInstallmentSelected = {},
    )
}
