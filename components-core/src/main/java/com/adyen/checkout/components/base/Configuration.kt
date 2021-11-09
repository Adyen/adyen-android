package com.adyen.checkout.components.base

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import java.util.*

abstract class Configuration protected constructor(
    val shopperLocale: Locale,
    val environment: Environment,
    val clientKey: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as Locale,
        parcel.readParcelable(Environment::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(shopperLocale)
        parcel.writeParcelable(environment, flags)
        parcel.writeString(clientKey)
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }
}
