/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams

internal data class CashAppPayComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val cashAppPayEnvironment: CashAppPayEnvironment,
    val returnUrl: String?,
    val showStorePaymentField: Boolean,
    val storePaymentMethod: Boolean,
    val clientId: String?,
    val scopeId: String?,
) : ComponentParams by commonComponentParams, ButtonParams {

    fun requireClientId(): String = requireNotNull(clientId)
}
