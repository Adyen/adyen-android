/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */

package com.adyen.checkout.qrcode

import com.adyen.checkout.components.base.OutputData

data class QRCodeOutputData(
    override val isValid: Boolean,
    val paymentMethodType: String?,
    val qrCodeData: String?,
) : OutputData
