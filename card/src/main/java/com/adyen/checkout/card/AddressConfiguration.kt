package com.adyen.checkout.card

import android.os.Parcel
import android.os.Parcelable

/**
 * Configuration class for Address Form in Card Component. This class can be used define the
 * visibility of the address form.
 */
sealed class AddressConfiguration : Parcelable {

    /**
     * Address Form will be hidden.
     */
    object None : AddressConfiguration() {
        @JvmField
        val CREATOR = object : Parcelable.Creator<None> {
            override fun createFromParcel(source: Parcel?) = None
            override fun newArray(size: Int) = arrayOfNulls<None>(size)
        }
        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel?, flags: Int) {
            // no ops
        }
    }

    /**
     * Only postal code will be shown as part of the card component.
     */
    object PostalCode : AddressConfiguration() {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PostalCode> {
            override fun createFromParcel(source: Parcel?) = PostalCode
            override fun newArray(size: Int) = arrayOfNulls<PostalCode>(size)
        }
        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel?, flags: Int) {
            // no ops
        }
    }

    /**
     * Full Address Form will be shown as part of the card component.
     *
     * @param defaultCountryCode Default country to be selected while initializing the form.
     * @param supportedCountryCodes Supported country codes to be filtered from the available country
     * options.
     */
    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList()
    ) : AddressConfiguration() {
        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<FullAddress> {
                @Suppress("UNCHECKED_CAST")
                override fun createFromParcel(source: Parcel) = FullAddress(
                    defaultCountryCode = source.readString(),
                    supportedCountryCodes = source.readArrayList(String::class.java.classLoader) as List<String>
                )
                override fun newArray(size: Int) = arrayOfNulls<FullAddress>(size)
            }
        }

        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(defaultCountryCode)
            dest.writeList(supportedCountryCodes)
        }
    }
}
