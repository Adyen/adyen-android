/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 1/11/2023.
 */

package com.adyen.checkout.components.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentMethodCustomDisplayInformation(
    val name: String
) : Parcelable
