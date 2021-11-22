/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */
package com.adyen.checkout.giftcard

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.GiftCardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.core.util.ParcelUtils

/**
 * PaymentComponentState for GiftCardComponent with additional data.
 */
class GiftCardComponentState(
    paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>,
    isInputValid: Boolean,
    isReady: Boolean,
    val lastFourDigits: String?
) : PaymentComponentState<GiftCardPaymentMethod>(paymentComponentData, isInputValid, isReady), Parcelable {

    private constructor(parcel: Parcel) : this(
        paymentComponentData = parcel.readParcelable(PaymentComponentData::class.java.classLoader)!!,
        isInputValid = ParcelUtils.readBoolean(parcel),
        isReady = ParcelUtils.readBoolean(parcel),
        lastFourDigits = parcel.readString()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(data, flags)
        ParcelUtils.writeBoolean(dest, isInputValid)
        ParcelUtils.writeBoolean(dest, isReady)
        dest.writeString(lastFourDigits)
    }

    override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GiftCardComponentState> {
            override fun createFromParcel(source: Parcel) = GiftCardComponentState(source)
            override fun newArray(size: Int) = arrayOfNulls<GiftCardComponentState>(size)
        }
    }
}
