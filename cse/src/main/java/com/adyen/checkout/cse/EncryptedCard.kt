/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */
package com.adyen.checkout.cse

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EncryptedCard(
    val encryptedCardNumber: String?,
    val encryptedExpiryMonth: String?,
    val encryptedExpiryYear: String?,
    val encryptedSecurityCode: String?,
) : Parcelable
