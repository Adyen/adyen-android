/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.CheckoutException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Identifies which host URL to be used for network calls.
 */
@SuppressWarnings("PMD.DataClass")
public final class Environment implements Parcelable {

    public static final Environment TEST;
    public static final Environment EUROPE;
    public static final Environment UNITED_STATES;
    public static final Environment AUSTRALIA;
    public static final Environment INDIA;
    public static final Environment APSE;

    /**
     * @deprecated Use the same live environment as your back end instead. You can find that value in your Customer Area.
     */
    @Deprecated
    public static final Environment LIVE;

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
            INDIA = new Environment(new URL("https://checkoutshopper-live-in.adyen.com/checkoutshopper/"));
            APSE = new Environment(new URL("https://checkoutshopper-live-apse.adyen.com/checkoutshopper/"));
            LIVE = EUROPE;
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
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @NonNull
    public String getBaseUrl() {
        return mBaseUrl.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Environment that = (Environment) o;
        return mBaseUrl.toString().equals(that.mBaseUrl.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBaseUrl);
    }
}
