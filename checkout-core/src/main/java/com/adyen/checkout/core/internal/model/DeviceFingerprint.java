/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 14/09/2017.
 */

package com.adyen.checkout.core.internal.model;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.adyen.checkout.base.internal.Base64Coder;
import com.adyen.checkout.base.internal.Json;
import com.adyen.checkout.base.internal.JsonEncodable;
import com.adyen.checkout.core.BuildConfig;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.base.internal.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public final class DeviceFingerprint extends JsonEncodable {
    private static final String DEVICE_FINGERPRINT_VERSION = "1.0";

    private static final String PLATFORM = "Android";

    private static final String OS_VERSION = String.valueOf(Build.VERSION.SDK_INT);

    private static final String SDK_VERSION = BuildConfig.VERSION_NAME;

    private static final String DEVICE_MODEL = Build.MANUFACTURER + " " + Build.DEVICE;

    private final String mDeviceIdentifier;

    private final String mIntegration;

    private final Locale mLocale;

    private final String mGenerationTime;

    @NonNull
    public static String generateSdkToken(@NonNull Context context, @NonNull String integrationType) throws CheckoutException {
        DeviceFingerprint deviceFingerprint = new DeviceFingerprint(context, integrationType);

        try {
            return Base64Coder.encode(deviceFingerprint, Base64.NO_WRAP);
        } catch (JSONException e) {
            throw new CheckoutException.Builder("Error generating SDK token.", e)
                    .setFatal(true)
                    .build();
        }
    }

    @SuppressWarnings("HardwareIds")
    private DeviceFingerprint(@NonNull Context context, @NonNull String integration) {
        mDeviceIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        mIntegration = integration;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mLocale = context.getResources().getConfiguration().locale;
        } else {
            mLocale = context.getResources().getConfiguration().getLocales().get(0);
        }

        mGenerationTime = Json.serializeDate(new Date());
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("deviceFingerprintVersion", DEVICE_FINGERPRINT_VERSION);
        deviceInfo.put("platform", PLATFORM);
        deviceInfo.put("osVersion", OS_VERSION);
        deviceInfo.put("sdkVersion", SDK_VERSION);
        deviceInfo.put("deviceModel", DEVICE_MODEL);
        deviceInfo.put("deviceIdentifier", mDeviceIdentifier);
        deviceInfo.put("integration", mIntegration);
        deviceInfo.put("locale", getLocaleWithoutScript(mLocale));
        deviceInfo.put("generationTime", mGenerationTime);

        return deviceInfo;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceFingerprint that = (DeviceFingerprint) o;

        if (mDeviceIdentifier != null ? !mDeviceIdentifier.equals(that.mDeviceIdentifier) : that.mDeviceIdentifier != null) {
            return false;
        }
        if (mIntegration != null ? !mIntegration.equals(that.mIntegration) : that.mIntegration != null) {
            return false;
        }
        if (mLocale != null ? !mLocale.equals(that.mLocale) : that.mLocale != null) {
            return false;
        }
        return mGenerationTime != null ? mGenerationTime.equals(that.mGenerationTime) : that.mGenerationTime == null;
    }

    @Override
    public int hashCode() {
        int result = mDeviceIdentifier != null ? mDeviceIdentifier.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mIntegration != null ? mIntegration.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mLocale != null ? mLocale.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mGenerationTime != null ? mGenerationTime.hashCode() : 0);
        return result;
    }

    /**
     * Some locales have extra part named "Language Script" that specified by # after Region code e.g. "zh_CN_#Hans"
     * Since backend doesn't support "script" part of locale, we need to remove it!
     * This function parse locale and return string with `${LanguageCode}_${RegionCode}` format.
     */
    private String getLocaleWithoutScript(@NonNull Locale locale) {
        return String.format("%s_%s", locale.getLanguage(), locale.getCountry());
    }
}
