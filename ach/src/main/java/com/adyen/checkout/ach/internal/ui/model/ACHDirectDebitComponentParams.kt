/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class ACHDirectDebitComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    override val isSubmitButtonVisible: Boolean,
    val addressParams: AddressParams
) : ComponentParams, ButtonParams
