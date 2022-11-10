/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.ui.order

import android.os.Parcelable
import com.adyen.checkout.components.model.connection.OrderPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class OrderModel(
    val orderData: String,
    val pspReference: String,
    val remainingAmount: Amount,
    val paymentMethods: List<OrderPaymentMethod>
) : Parcelable
