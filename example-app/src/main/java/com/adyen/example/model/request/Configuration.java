/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.example.model.request;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import java.io.Serializable;

public final class Configuration implements Serializable {
    @Json(name = "installments")
    private Installments mInstallments;

    @Json(name = "cardHolderName")
    private CardHolderNameRequirement mCardHolderName;

    @Json(name = "shopperInput")
    private ShopperInput mShopperInput;

    @Nullable
    public CardHolderNameRequirement getCardHolderName() {
        return mCardHolderName;
    }

    @Nullable
    public Installments getInstallments() {
        return mInstallments;
    }

    @Nullable
    public ShopperInput getShopperInput() {
        return mShopperInput;
    }

    public void setCardHolderName(@Nullable CardHolderNameRequirement cardHolderName) {
        mCardHolderName = cardHolderName;
    }

    public void setInstallments(@Nullable Installments installments) {
        mInstallments = installments;
    }

    public void setShopperInput(@Nullable ShopperInput shopperInput) {
        mShopperInput = shopperInput;
    }

    public enum CardHolderNameRequirement {
        NONE,
        OPTIONAL,
        REQUIRED
    }
}


