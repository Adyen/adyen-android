/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/6/2019.
 */

package com.adyen.checkout.components.analytics;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.BuildConfig;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.LocaleUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class AnalyticEvent implements Parcelable {

    private static final String DROPIN_FLAVOR = "dropin";
    private static final String COMPONENT_FLAVOR = "components";

    private static final String CURRENT_PAYLOAD_VERSION = "1";
    private static final String ANDROID_PLATFORM = "android";

    private static final String PAYLOAD_VERSION_KEY = "payload_version";
    private static final String VERSION_KEY = "version";
    private static final String FLAVOR_KEY = "flavor";
    private static final String COMPONENT_KEY = "component";
    private static final String LOCALE_KEY = "locale";
    private static final String PLATFORM_KEY = "platform";

    private static final String REFERER_KEY = "referer";
    private static final String DEVICE_BRAND_KEY = "device_brand";
    private static final String DEVICE_MODEL_KEY = "device_model";
    private static final String SYSTEM_VERSION_KEY = "system_version";

    public enum Flavor {
        DROPIN,
        COMPONENT
    }

    private final String mPayloadVersion = CURRENT_PAYLOAD_VERSION;
    private final String mVersion = BuildConfig.CHECKOUT_VERSION;
    // e.g: 'dropin', 'component'
    private final String mFlavor;
    // e.g: dropin, paymentType
    private final String mComponent;
    // e.g: en_US
    private final String mLocale;
    private final String mPlatform = ANDROID_PLATFORM;

    // e.g: package name
    private final String mReferer;
    private final String mDeviceBrand = Build.BRAND;
    private final String mDeviceModel = Build.MODEL;
    private final String mSystemVersion = String.valueOf(Build.VERSION.SDK_INT);

    public static final Creator<AnalyticEvent> CREATOR = new Creator<AnalyticEvent>() {
        @Override
        public AnalyticEvent createFromParcel(Parcel in) {
            return new AnalyticEvent(in);
        }

        @Override
        public AnalyticEvent[] newArray(int size) {
            return new AnalyticEvent[size];
        }
    };

    /**
     * Create an AnalyticEvent representing a state of the usage of the components.
     *
     * @param context A context to get the package name.
     * @param flavor One of the available flavors os integration.
     * @param components The component that was openend.
     * @param locale The user locale being used.
     * @return A new instance of an AnalyticEvent
     */
    @NonNull
    public static AnalyticEvent create(@NonNull Context context, @NonNull Flavor flavor, @NonNull String components, @NonNull Locale locale) {
        final String flavorName;
        switch (flavor) {
            case DROPIN:
                flavorName = DROPIN_FLAVOR;
                break;
            case COMPONENT:
                flavorName = COMPONENT_FLAVOR;
                break;
            default:
                throw new CheckoutException("Unexpected flavor - " + flavor.name());
        }
        return new AnalyticEvent(context.getPackageName(), flavorName, components, LocaleUtil.toLanguageTag(locale));
    }

    AnalyticEvent(@NonNull Parcel in) {
        mFlavor = in.readString();
        mComponent = in.readString();
        mLocale = in.readString();
        mReferer = in.readString();
    }

    private AnalyticEvent(@NonNull String packageName, @NonNull String flavor, @NonNull String components, @NonNull String locale) {
        mReferer = packageName;
        mLocale = locale;
        mFlavor = flavor;
        mComponent = components;
    }

    /**
     * Puts the event in the form of GET parameters in front of the provided base URL.
     *
     * @param baseUrl A base URL of the endpoint to send the events to.
     * @return The full URL to be called.
     */
    @NonNull
    URL toUrl(@NonNull String baseUrl) throws MalformedURLException {
        if (!URLUtil.isValidUrl(baseUrl)) {
            throw new MalformedURLException("Invalid URL format - " + baseUrl);
        }
        final Uri baseUri = Uri.parse(baseUrl);

        final Uri finalUri = new Uri.Builder()
                .scheme(baseUri.getScheme())
                .authority(baseUri.getAuthority())
                .path(baseUri.getPath())
                .appendQueryParameter(PAYLOAD_VERSION_KEY, mPayloadVersion)
                .appendQueryParameter(VERSION_KEY, mVersion)
                .appendQueryParameter(FLAVOR_KEY, mFlavor)
                .appendQueryParameter(COMPONENT_KEY, mComponent)
                .appendQueryParameter(LOCALE_KEY, mLocale)
                .appendQueryParameter(PLATFORM_KEY, mPlatform)
                .appendQueryParameter(REFERER_KEY, mReferer)
                .appendQueryParameter(DEVICE_BRAND_KEY, mDeviceBrand)
                .appendQueryParameter(DEVICE_MODEL_KEY, mDeviceModel)
                .appendQueryParameter(SYSTEM_VERSION_KEY, mSystemVersion)
                .build();

        return new URL(finalUri.toString());
    }

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mFlavor);
        dest.writeString(mComponent);
        dest.writeString(mLocale);
        dest.writeString(mReferer);
    }

}
