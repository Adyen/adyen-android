package com.adyen.core;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.adyen.core.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * Class that creates the token that should be sent with the payment setup request.
 */
final class DeviceTokenGenerator {

    private static final String TAG = DeviceTokenGenerator.class.getSimpleName();

    private static final String DEVICE_FINGER_PRINT_VERSION = "1.1";
    private static final String SDK_VERSION = BuildConfig.VERSION_NAME;

    private DeviceTokenGenerator() {
        // Private Constructor
    }

    /**
     * Uses device and SDK information to create token.
     * @return token
     */
    static String getToken(final Context context, final PaymentStateHandler paymentStateHandler, boolean isQuickIntegration) {
        final JSONObject deviceInfo = new JSONObject();
        try {
            final String androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            deviceInfo.put("deviceFingerprintVersion", DEVICE_FINGER_PRINT_VERSION);
            deviceInfo.put("platform", "android");
            deviceInfo.put("apiVersion", "6");
            deviceInfo.put("osVersion", Build.VERSION.SDK_INT);
            deviceInfo.put("sdkVersion", SDK_VERSION);
            deviceInfo.put("deviceIdentifier", androidId);
            deviceInfo.put("locale", StringUtils.getLocale(context));
            deviceInfo.put("integration", (isQuickIntegration) ? "quick" : "custom");
            deviceInfo.put("deviceModel", Build.MANUFACTURER + " " + Build.DEVICE);

        } catch (final JSONException jsonException) {
            Log.e(TAG, "Token could not be created", jsonException);
            paymentStateHandler.setPaymentErrorThrowableAndTriggerError(jsonException);
            return "";
        }
        return Base64.encodeToString(deviceInfo.toString().getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);
    }
}
