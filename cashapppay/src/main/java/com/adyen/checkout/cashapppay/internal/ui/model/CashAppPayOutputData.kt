/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.OutputData

internal data class CashAppPayOutputData(
    val isStorePaymentSelected: Boolean,
    val authorizationData: CashAppPayAuthorizationData?,
) : OutputData {

    override val isValid: Boolean
        get() = authorizationData != null
}

internal data class CashAppPayAuthorizationData(
    val oneTimeData: CashAppPayOneTimeData?,
    val onFileData: CashAppPayOnFileData?,
)

internal data class CashAppPayOneTimeData(
    val grantId: String?,
    val customerId: String?,
)

internal data class CashAppPayOnFileData(
    val grantId: String?,
    val cashTag: String?,
    val customerId: String?,
)
