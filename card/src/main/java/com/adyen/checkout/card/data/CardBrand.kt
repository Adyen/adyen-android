package com.adyen.checkout.card.data

import android.os.Parcel
import android.os.Parcelable

data class CardBrand(val txVariant: String) : Parcelable {

    internal var cardType: CardType? = CardType.getByBrandName(txVariant)
        private set

    /**
     * Use this constructor when defining the supported card type predefined inside [CardType] enum
     * inside your component
     */
    constructor(cardType: CardType) : this(cardType.txVariant) {
        this.cardType = cardType
    }

    private constructor(parcel: Parcel) : this(parcel.readString().orEmpty())

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(txVariant)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<CardBrand> {
            override fun createFromParcel(parcel: Parcel): CardBrand {
                return CardBrand(parcel)
            }

            override fun newArray(size: Int): Array<CardBrand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
