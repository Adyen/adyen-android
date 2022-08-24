/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher

import com.adyen.checkout.components.base.OutputData

data class VoucherOutputData(
    override val isValid: Boolean,
    val paymentMethodType: String?,
    val downloadUrl: String?,
) : OutputData
