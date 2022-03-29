/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */
package com.adyen.checkout.cse

import android.os.Parcel
import android.os.Parcelable

data class EncryptedCard(
    val encryptedCardNumber: String?,
    val encryptedExpiryMonth: String?,
    val encryptedExpiryYear: String?,
    val encryptedSecurityCode: String?,
) : Parcelable {

    private constructor(source: Parcel) : this(
        encryptedCardNumber = source.readString(),
        encryptedExpiryMonth = source.readString(),
        encryptedExpiryYear = source.readString(),
        encryptedSecurityCode = source.readString(),
    )

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(encryptedCardNumber)
        dest.writeString(encryptedExpiryMonth)
        dest.writeString(encryptedExpiryYear)
        dest.writeString(encryptedSecurityCode)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EncryptedCard> = object : Parcelable.Creator<EncryptedCard> {
            override fun createFromParcel(source: Parcel): EncryptedCard {
                return EncryptedCard(source)
            }

            override fun newArray(size: Int): Array<EncryptedCard?> {
                return arrayOfNulls(size)
            }
        }
    }
}
