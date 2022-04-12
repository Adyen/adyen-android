package com.adyen.checkout.card

import android.os.Parcel
import android.os.Parcelable

// TODO docs
sealed class AddressConfiguration: Parcelable {

    object None: AddressConfiguration() {
        @JvmField
        val CREATOR = object : Parcelable.Creator<None> {
            override fun createFromParcel(source: Parcel?) = None
            override fun newArray(size: Int) = arrayOfNulls<None>(size)
        }
        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel?, flags: Int) {}
    }

    object PostalCode: AddressConfiguration() {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PostalCode> {
            override fun createFromParcel(source: Parcel?) = PostalCode
            override fun newArray(size: Int) = arrayOfNulls<PostalCode>(size)
        }
        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel?, flags: Int) {}
    }

    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList()
    ): AddressConfiguration() {
        @JvmField
        val CREATOR = object : Parcelable.Creator<FullAddress> {
            @Suppress("UNCHECKED_CAST")
            override fun createFromParcel(source: Parcel) = FullAddress(
                defaultCountryCode = source.readString(),
                supportedCountryCodes = source.readArrayList(String()::class.java.classLoader) as List<String>
            )
            override fun newArray(size: Int) = arrayOfNulls<FullAddress>(size)
        }
        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(defaultCountryCode)
            dest.writeList(supportedCountryCodes)
        }
    }
}