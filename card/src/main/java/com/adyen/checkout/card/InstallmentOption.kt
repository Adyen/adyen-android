package com.adyen.checkout.card

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.core.util.ParcelUtils

enum class InstallmentOption(val type: String?) {
    ONE_TIME(null),
    REGULAR("regular"),
    REVOLVING("revolving")
}

data class InstallmentConfiguration(
    val defaultOptions: InstallmentOptions.DefaultInstallmentOptions?,
    val cardBasedOptions: List<InstallmentOptions.CardBasedInstallmentOptions>?
) : Parcelable {

    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(
        parcel.readParcelable(InstallmentOptions.DefaultInstallmentOptions::class.java.classLoader),
        parcel.readArrayList(InstallmentOptions.CardBasedInstallmentOptions::class.java.classLoader)
            as? List<InstallmentOptions.CardBasedInstallmentOptions>
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

/**
 * @param values Monthly installment options (e.g. {2, 3, 4, 6})
 * @param includeRevolving Whether revolving installment should be included as an option
 */
sealed class InstallmentOptions(
    open val values: List<Int>,
    open val includeRevolving: Boolean
) : Parcelable {

    companion object {
        private const val STARTING_INSTALLMENT_VALUE = 2
    }

    /**
     * @param values see [InstallmentOptions.values]
     * @param includeRevolving see [InstallmentOptions.includeRevolving]
     * @param cardType a [CardType] to apply the given options
     */
    data class CardBasedInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean,
        val cardType: CardType
    ) : InstallmentOptions(values, includeRevolving) {

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<CardBasedInstallmentOptions> {
                override fun createFromParcel(source: Parcel) = CardBasedInstallmentOptions(source)
                override fun newArray(size: Int) = arrayOfNulls<CardBasedInstallmentOptions>(size)
            }
        }

        /**
         * @param maxInstallments Maximum number of installments
         *
         * Creates a [DefaultInstallmentOptions] instance with values in range [2, maxInstallments]
         */
        constructor(maxInstallments: Int, includeRevolving: Boolean, cardType: CardType):
            this((STARTING_INSTALLMENT_VALUE..maxInstallments).toList(), includeRevolving, cardType)

        @Suppress("UNCHECKED_CAST")
        private constructor(parcel: Parcel) : this(
            parcel.readArrayList(Int::class.java.classLoader) as List<Int>,
            ParcelUtils.readBoolean(parcel),
            parcel.readSerializable() as CardType
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeSerializable(cardType)
        }

        override fun describeContents() = Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    /**
     * @param values see [InstallmentOptions.values]
     * @param includeRevolving see [InstallmentOptions.includeRevolving]
     */
    data class DefaultInstallmentOptions(
        override val values: List<Int>,
        override val includeRevolving: Boolean
    ) : InstallmentOptions(values, includeRevolving) {

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<DefaultInstallmentOptions> {
                override fun createFromParcel(source: Parcel) = DefaultInstallmentOptions(source)
                override fun newArray(size: Int) = arrayOfNulls<DefaultInstallmentOptions>(size)
            }
        }

        /**
         * @param maxInstallments Maximum number of installments
         *
         * Creates a [DefaultInstallmentOptions] instance with values in range [2, maxInstallments]
         */
        constructor(maxInstallments: Int, includeRevolving: Boolean):
            this((STARTING_INSTALLMENT_VALUE..maxInstallments).toList(), includeRevolving)

        @Suppress("UNCHECKED_CAST")
        private constructor(parcel: Parcel) : this(
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
