/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.OutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class VoucherOutputData(
    override val isValid: Boolean,
    val paymentMethodType: String?,
    val downloadUrl: String?,
    val expiresAt: String?,
    val reference: String?,
    val totalAmount: Amount?,
) : OutputData
