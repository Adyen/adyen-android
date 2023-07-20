/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class CashAppPayComponentParams(
    override val isSubmitButtonVisible: Boolean,
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsParams: AnalyticsParams,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    val cashAppPayEnvironment: CashAppPayEnvironment,
    val returnUrl: String?,
    val showStorePaymentField: Boolean,
    val storePaymentMethod: Boolean,
    val clientId: String?,
    val scopeId: String?,
) : ComponentParams, ButtonParams {

    fun requireClientId(): String = requireNotNull(clientId)
}
