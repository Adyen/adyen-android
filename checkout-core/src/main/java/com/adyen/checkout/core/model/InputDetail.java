package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject.SerializedName;

import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/08/2017.
 */
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
     * @return The {@link Item Items} of this {@link InputDetail}. Only present for {@link Type#SELECT}.
     */
    @Nullable
    List<Item> getItems();

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
        @SerializedName("emailAddress")
        EMAIL_ADDRESS,
        @SerializedName("payWithGoogleToken")
        GOOGLE_PAY_TOKEN,
        IBAN,
        RADIO,
        SELECT,
        @SerializedName("samsungPayToken")
        SAMSUNG_PAY_TOKEN,
        @SerializedName("cvc")
        SECURITY_CODE,
        TEXT
    }
}
