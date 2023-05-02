/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */

package com.adyen.checkout.qrcode.internal.ui.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import com.adyen.checkout.components.core.internal.ui.model.OutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class QRCodeOutputData(
    override val isValid: Boolean,
    val paymentMethodType: String?,
    val qrCodeData: String?,
    val qrImageUrl: String? = null,
    @StringRes val messageTextResource: Int? = null
) : OutputData
