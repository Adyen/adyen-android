/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.ui.giftcard

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.Amount
import java.util.Locale

data class GiftCardPaymentConfirmationData(
    val amountPaid: Amount,
    val remainingBalance: Amount,
    val shopperLocale: Locale,
    val brand: String,
    val lastFourDigits: String
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        amountPaid = parcel.readParcelable(Amount::class.java.classLoader)!!,
        remainingBalance = parcel.readParcelable(Amount::class.java.classLoader)!!,
        shopperLocale = parcel.readSerializable() as Locale,
        brand = parcel.readString().orEmpty(),
        lastFourDigits = parcel.readString().orEmpty()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(amountPaid, flags)
        dest.writeParcelable(remainingBalance, flags)
        dest.writeSerializable(shopperLocale)
        dest.writeString(brand)
        dest.writeString(lastFourDigits)
    }

    override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GiftCardPaymentConfirmationData> {
            override fun createFromParcel(source: Parcel) = GiftCardPaymentConfirmationData(source)
            override fun newArray(size: Int) = arrayOfNulls<GiftCardPaymentConfirmationData>(size)
        }
    }
}
