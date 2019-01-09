/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/08/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject.SerializedName;
import com.adyen.checkout.core.CheckoutException;

import java.util.List;

public interface InputDetail extends Parcelable {
    /**
     * @return The key of this {@link InputDetail}.
     */
    @NonNull
    String getKey();

    /**
     * @return The {@link Type} of this {@link InputDetail}.
     */
    @NonNull
    Type getType();

    /**
     * @return Whether this {@link InputDetail} is optional.
     */
    boolean isOptional();

    /**
     * @return The pre-filled value of this {@link InputDetail}.
     */
    @Nullable
    String getValue();

    /**
     * @return The {@link Item Items} that populate this {@link InputDetail}. Only present for {@link Type#SELECT}.
     */
    @Nullable
    List<Item> getItems();

    /**
     * @return the {@link Configuration} for this {@link InputDetail}.
     */
    @Nullable
    <T extends Configuration> T getConfiguration(@NonNull Class<T> clazz) throws CheckoutException;

    /**
     * @return the list of {@link InputDetail} if this input is of type {@link Type#FIELD_SET} and represents a group od details.
     */
    @Nullable
    List<InputDetail> getChildInputDetails();

    /**
     * The type of the {@link InputDetail}.
     */
    enum Type {
        ADDRESS,
        @SerializedName("androidPayToken")
        ANDROID_PAY_TOKEN,
        BOOLEAN,
        @SerializedName("cardToken")
        CARD_TOKEN,
        DATE,
        @SerializedName("emailAddress")
        EMAIL_ADDRESS,
        //fieldSet represents a grouped set of more InputDetails
        @SerializedName("fieldSet")
        FIELD_SET,
        @SerializedName("payWithGoogleToken")
        GOOGLE_PAY_TOKEN,
        IBAN,
        RADIO,
        SELECT,
        @SerializedName("samsungPayToken")
        SAMSUNG_PAY_TOKEN,
        @SerializedName("cvc")
        SECURITY_CODE,
        @SerializedName("tel")
        TELEPHONE,
        TEXT
    }
}
