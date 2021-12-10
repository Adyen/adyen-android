/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.ui.order

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.connection.OrderPaymentMethod
import com.adyen.checkout.components.model.payments.Amount

data class OrderModel(
    val orderData: String,
    val pspReference: String,
    val remainingAmount: Amount,
    val paymentMethods: List<OrderPaymentMethod>
) : Parcelable {

    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(
        orderData = parcel.readString().orEmpty(),
        pspReference = parcel.readString().orEmpty(),
        remainingAmount = parcel.readParcelable(Amount::class.java.classLoader)!!,
        paymentMethods = parcel.readArrayList(OrderPaymentMethod::class.java.classLoader) as List<OrderPaymentMethod>
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<OrderModel> {
            override fun createFromParcel(source: Parcel) = OrderModel(source)
            override fun newArray(size: Int) = arrayOfNulls<OrderModel>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(orderData)
        dest.writeString(pspReference)
        dest.writeParcelable(remainingAmount, flags)
        dest.writeList(paymentMethods)
    }

    override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
}
