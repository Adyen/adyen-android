/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import java.util.Locale

@Composable
internal fun InstallmentField(
    installmentState: InstallmentViewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ValuePickerField(
        value = installmentState.selectedOption?.toDisplayText() ?: "",
        label = resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_TITLE),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun InstallmentModel.toDisplayText(): String {
    return when (option) {
        InstallmentOption.ONE_TIME ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_ONE_TIME)

        InstallmentOption.REVOLVING ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REVOLVING)

        InstallmentOption.REGULAR -> {
            val count = numberOfInstallments ?: 1
            if (showAmount && amount != null) {
                resolveString(
                    CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR_WITH_PRICE,
                    count,
                    amount.format(shopperLocale),
                )
            } else {
                resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR, count)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InstallmentFieldPreview() {
    val options = listOf(
        InstallmentModel(null, InstallmentOption.ONE_TIME, null, Locale.US, false),
        InstallmentModel(2, InstallmentOption.REGULAR, null, Locale.US, false),
        InstallmentModel(3, InstallmentOption.REGULAR, null, Locale.US, false),
        InstallmentModel(1, InstallmentOption.REVOLVING, null, Locale.US, false),
    )
    InstallmentField(
        installmentState = InstallmentViewState(options = options, selectedOption = options.first()),
        onClick = {},
    )
}
