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
import com.adyen.checkout.card.internal.ui.model.toDisplayText
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.ui.internal.element.SelectableListItem
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun InstallmentPicker(
    installmentOptions: List<InstallmentModel>,
    selectedInstallment: InstallmentModel?,
    onItemClick: (InstallmentModel) -> Unit,
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
                    isSelected = option == selectedInstallment,
                    onClick = { onItemClick(option) },
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun InstallmentPickerPreview() {
    val options = listOf(
        InstallmentModel.OneTime,
        InstallmentModel.Revolving,
        InstallmentModel.Regular(
            numberOfInstallments = 2,
            amountPerInstallment = null,
            showAmount = false,
        ),
        InstallmentModel.Regular(
            numberOfInstallments = 3,
            amountPerInstallment = Amount("EUR", 100),
            showAmount = true,
        ),
    )
    InstallmentPicker(
        installmentOptions = options,
        selectedInstallment = options.first(),
        onItemClick = {},
    )
}
