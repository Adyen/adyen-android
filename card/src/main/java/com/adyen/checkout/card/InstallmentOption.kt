package com.adyen.checkout.card

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.core.util.ParcelUtils

enum class InstallmentOption(type: String?) {
    ONE_TIME(null),
    REGULAR("regular"),
    REVOLVING("revolving")
}

data class InstallmentConfiguration(
    val defaultOptions: InstallmentOptions.DefaultInstallmentOptions?,
    val cardBasedOptions: List<InstallmentOptions.CardBasedInstallmentOptions>?
): Parcelable {

    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel): this(
        parcel.readParcelable(InstallmentOptions.DefaultInstallmentOptions::class.java.classLoader),
        parcel.readArrayList(InstallmentOptions.CardBasedInstallmentOptions::class.java.classLoader) as List<InstallmentOptions.CardBasedInstallmentOptions>
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<InstallmentConfiguration> {
            override fun createFromParcel(source: Parcel) = InstallmentConfiguration(source)
            override fun newArray(size: Int) = arrayOfNulls<InstallmentConfiguration>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(defaultOptions, flags)
        dest.writeList(cardBasedOptions)
    }

    override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR

}

sealed class InstallmentOptions(
    open val values: List<Int>,
    open val includeRevolving: Boolean
): Parcelable {

    data class CardBasedInstallmentOptions(
        val cardType: CardType,
        override val values: List<Int>,
        override val includeRevolving: Boolean
    ): InstallmentOptions(values, includeRevolving) {

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<CardBasedInstallmentOptions> {
                override fun createFromParcel(source: Parcel) = CardBasedInstallmentOptions(source)
                override fun newArray(size: Int) = arrayOfNulls<CardBasedInstallmentOptions>(size)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private constructor(parcel: Parcel): this(
            parcel.readSerializable() as CardType,
            parcel.readArrayList(Int::class.java.classLoader) as List<Int>,
            ParcelUtils.readBoolean(parcel)
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeSerializable(cardType)
        }

        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    data class DefaultInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean
    ): InstallmentOptions(values, includeRevolving) {

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<DefaultInstallmentOptions> {
                override fun createFromParcel(source: Parcel) = DefaultInstallmentOptions(source)
                override fun newArray(size: Int) = arrayOfNulls<DefaultInstallmentOptions>(size)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private constructor(parcel: Parcel): this(
            parcel.readArrayList(Int::class.java.classLoader) as List<Int>,
            ParcelUtils.readBoolean(parcel)
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
        }

        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(values)
        ParcelUtils.writeBoolean(dest, includeRevolving)
    }

}