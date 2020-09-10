/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/3/2019.
 */

package com.adyen.checkout.core.api;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.ParcelUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Identifies which host URL to be used for network calls.
 */
@SuppressWarnings("PMD.DataClass")
public final class Environment implements Parcelable {

    public static final Environment TEST;
    public static final Environment EUROPE;
    public static final Environment UNITED_STATES;
    public static final Environment AUSTRALIA;

    public static final Creator<Environment> CREATOR = new Creator<Environment>() {
        @Override
        public Environment createFromParcel(@NonNull Parcel in) {
            return new Environment(in);
        }

        @Override
        public Environment[] newArray(int size) {
            return new Environment[size];
        }
    };

    static {
        try {
            TEST = new Environment(new URL("https://checkoutshopper-test.adyen.com/checkoutshopper/"));
            EUROPE = new Environment(new URL("https://checkoutshopper-live.adyen.com/checkoutshopper/"));
            UNITED_STATES = new Environment(new URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/"));
            AUSTRALIA = new Environment(new URL("https://checkoutshopper-live-au.adyen.com/checkoutshopper/"));
        } catch (MalformedURLException e) {
            throw new CheckoutException("Failed to parse Environment URL.", e);
        }
    }

    private final URL mBaseUrl;

    public Environment(@NonNull URL baseUrl) {
        mBaseUrl = baseUrl;
    }

    Environment(@NonNull Parcel in) {
        mBaseUrl = (URL) in.readSerializable();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeSerializable(mBaseUrl);
    }

    @Override
    public int describeContents() {
        return ParcelUtils.NO_FILE_DESCRIPTOR;
    }

    @NonNull
    public String getBaseUrl() {
        return mBaseUrl.toString();
    }
}
