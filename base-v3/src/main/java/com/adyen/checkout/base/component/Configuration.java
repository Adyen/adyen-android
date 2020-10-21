/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/7/2019.
 */

package com.adyen.checkout.base.component;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.util.ParcelUtils;

import java.util.Locale;

public abstract class Configuration implements Parcelable {

    public static final String DEFAULT_EMPTY_CLIENT_KEY = "";

    private final Locale mShopperLocale;
    private final Environment mEnvironment;
    private final String mClientKey;

    protected Configuration(@NonNull Locale shopperLocale, @NonNull Environment environment, @Nullable String clientKey) {
        mShopperLocale = shopperLocale;
        mEnvironment = environment;
        mClientKey = clientKey;
    }

    protected Configuration(@NonNull Parcel in) {
        mShopperLocale = (Locale) in.readSerializable();
        mEnvironment = in.readParcelable(Environment.class.getClassLoader());
        mClientKey = in.readString();
    }

    @NonNull
    public Environment getEnvironment() {
        return mEnvironment;
    }

    @NonNull
    public Locale getShopperLocale() {
        return mShopperLocale;
    }

    @NonNull
    public String getClientKey() {
        return mClientKey;
    }

    @Override
    public int describeContents() {
        return ParcelUtils.NO_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeSerializable(mShopperLocale);
        dest.writeParcelable(mEnvironment, flags);
        dest.writeString(mClientKey);
    }
}
