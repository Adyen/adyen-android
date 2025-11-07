/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/11/2023.
 */

package com.adyen.checkout.dropin.old.internal.ui.model

import android.os.Parcelable
import com.adyen.checkout.components.core.PaymentMethod
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class DropInPaymentMethodInformation(
    val name: String
) : Parcelable

internal fun PaymentMethod.overrideInformation(information: DropInPaymentMethodInformation) {
    name = information.name
}
