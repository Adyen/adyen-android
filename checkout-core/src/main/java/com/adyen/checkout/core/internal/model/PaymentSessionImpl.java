package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.adyen.checkout.base.HostProvider;
import com.adyen.checkout.base.internal.Api;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/07/2018.
 */
public final class PaymentSessionImpl extends JsonObject implements PaymentSession {
    public static final Parcelable.Creator<PaymentSessionImpl> CREATOR = new DefaultCreator<>(PaymentSessionImpl.class);

    private static final String KEY_GENERATIONTIME = "generationtime";

    private static final String KEY_CHECKOUTSHOPPER_BASE_URL = "checkoutshopperBaseUrl";

    private static final String KEY_INITIATION_URL = "initiationUrl";

    private static final String KEY_DISABLE_RECURRING_DETAIL_URL = "disableRecurringDetailUrl";

    private static final String KEY_PAYMENT_DATA = "paymentData";

    private static final String KEY_PAYMENT = "payment";

    private static final String KEY_ENVIRONMENT = "environment";

    private static final String KEY_PAYMENT_METHODS = "paymentMethods";

    private static final String KEY_PUBLIC_KEY = "publicKey";

    private static final String KEY_ONE_CLICK_PAYMENT_METHODS = "oneClickPaymentMethods";

    private final Date mGenerationTime;

    private final String mCheckoutshopperBaseUrl;

    private final String mInitiationUrl;

    private final String mDisableRecurringDetailUrl;

    private final String mPaymentData;

    private final PaymentImpl mPayment;

    private final String mEnvironment;

    private final String mPublicKey;

    private final List<PaymentMethodImpl> mPaymentMethods;

    private final List<PaymentMethodImpl> mOneClickPaymentMethods;

    @NonNull
    public static PaymentSessionImpl decode(@NonNull String encodedPaymentSession) throws CheckoutException {
        try {
            // Check if the whole PaymentSessionResponse was forwarded.
            JSONObject jsonObjectWrapper = new JSONObject(encodedPaymentSession);
            PaymentSessionResponse paymentSessionResponse = JsonObject.parseFrom(jsonObjectWrapper, PaymentSessionResponse.class);
            byte[] decodedPaymentSession = Base64.decode(paymentSessionResponse.getPaymentSession(), Base64.DEFAULT);
            String paymentSessionJson = new String(decodedPaymentSession, Api.CHARSET);
            JSONObject jsonObject = new JSONObject(paymentSessionJson);

            return parseFrom(jsonObject, PaymentSessionImpl.class);
        } catch (JSONException | IllegalArgumentException e1) {
            try {
                // Check if only the paymentSession value was forwarded.
                byte[] decodedPaymentSession = Base64.decode(encodedPaymentSession, Base64.DEFAULT);
                String paymentSessionJson = new String(decodedPaymentSession, Api.CHARSET);
                JSONObject jsonObject = new JSONObject(paymentSessionJson);

                return parseFrom(jsonObject, PaymentSessionImpl.class);
            } catch (IllegalArgumentException | JSONException e2) {
                throw new CheckoutException.Builder("Error parsing payment session data.", e2)
                        .setFatal(true)
                        .build();
            }
        }
    }

    private PaymentSessionImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mGenerationTime = parseDate(KEY_GENERATIONTIME);
        mCheckoutshopperBaseUrl = jsonObject.getString(KEY_CHECKOUTSHOPPER_BASE_URL);
        mInitiationUrl = jsonObject.getString(KEY_INITIATION_URL);
        mDisableRecurringDetailUrl = jsonObject.getString(KEY_DISABLE_RECURRING_DETAIL_URL);
        mPaymentData = jsonObject.getString(KEY_PAYMENT_DATA);
        mPayment = parse(KEY_PAYMENT, PaymentImpl.class);
        mEnvironment = jsonObject.optString(KEY_ENVIRONMENT);
        mPaymentMethods = parseList(KEY_PAYMENT_METHODS, PaymentMethodImpl.class);

        mPublicKey = jsonObject.optString(KEY_PUBLIC_KEY, null);
        mOneClickPaymentMethods = parseOptionalList(KEY_ONE_CLICK_PAYMENT_METHODS, PaymentMethodImpl.class);
    }

    @NonNull
    @Override
    public PaymentImpl getPayment() {
        return mPayment;
    }

    @NonNull
    @Override
    public List<PaymentMethod> getPaymentMethods() {
        return new ArrayList<PaymentMethod>(mPaymentMethods);
    }

    @Nullable
    @Override
    public List<PaymentMethod> getOneClickPaymentMethods() {
        return mOneClickPaymentMethods != null ? new ArrayList<PaymentMethod>(mOneClickPaymentMethods) : null;
    }

    @Nullable
    @Override
    public String getPublicKey() {
        return mPublicKey;
    }

    @NonNull
    @Override
    public Date getGenerationTime() {
        return mGenerationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentSessionImpl that = (PaymentSessionImpl) o;

        if (mGenerationTime != null ? !mGenerationTime.equals(that.mGenerationTime) : that.mGenerationTime != null) {
            return false;
        }
        if (mCheckoutshopperBaseUrl != null ? !mCheckoutshopperBaseUrl.equals(that.mCheckoutshopperBaseUrl) : that.mCheckoutshopperBaseUrl != null) {
            return false;
        }
        if (mInitiationUrl != null ? !mInitiationUrl.equals(that.mInitiationUrl) : that.mInitiationUrl != null) {
            return false;
        }
        if (mDisableRecurringDetailUrl != null ? !mDisableRecurringDetailUrl.equals(that.mDisableRecurringDetailUrl) : that
                .mDisableRecurringDetailUrl != null) {
            return false;
        }
        if (mPaymentData != null ? !mPaymentData.equals(that.mPaymentData) : that.mPaymentData != null) {
            return false;
        }
        if (mPayment != null ? !mPayment.equals(that.mPayment) : that.mPayment != null) {
            return false;
        }
        if (mEnvironment != null ? !mEnvironment.equals(that.mEnvironment) : that.mEnvironment != null) {
            return false;
        }
        if (mPublicKey != null ? !mPublicKey.equals(that.mPublicKey) : that.mPublicKey != null) {
            return false;
        }
        if (mPaymentMethods != null ? !mPaymentMethods.equals(that.mPaymentMethods) : that.mPaymentMethods != null) {
            return false;
        }
        return mOneClickPaymentMethods != null ? mOneClickPaymentMethods.equals(that.mOneClickPaymentMethods) : that.mOneClickPaymentMethods == null;
    }

    @Override
    public int hashCode() {
        int result = mGenerationTime != null ? mGenerationTime.hashCode() : 0;
        result = 31 * result + (mCheckoutshopperBaseUrl != null ? mCheckoutshopperBaseUrl.hashCode() : 0);
        result = 31 * result + (mInitiationUrl != null ? mInitiationUrl.hashCode() : 0);
        result = 31 * result + (mDisableRecurringDetailUrl != null ? mDisableRecurringDetailUrl.hashCode() : 0);
        result = 31 * result + (mPaymentData != null ? mPaymentData.hashCode() : 0);
        result = 31 * result + (mPayment != null ? mPayment.hashCode() : 0);
        result = 31 * result + (mEnvironment != null ? mEnvironment.hashCode() : 0);
        result = 31 * result + (mPublicKey != null ? mPublicKey.hashCode() : 0);
        result = 31 * result + (mPaymentMethods != null ? mPaymentMethods.hashCode() : 0);
        result = 31 * result + (mOneClickPaymentMethods != null ? mOneClickPaymentMethods.hashCode() : 0);
        return result;
    }

    @Nullable
    public List<PaymentMethodImpl> getOneClickPaymentMethodImpls() {
        return mOneClickPaymentMethods;
    }

    @NonNull
    public List<PaymentMethodImpl> getPaymentMethodImpls() {
        return mPaymentMethods;
    }

    @NonNull
    public String getPaymentData() {
        return mPaymentData;
    }

    @Nullable
    public String getEnvironment() {
        return mEnvironment;
    }

    @NonNull
    public String getCheckoutshopperBaseUrl() {
        return mCheckoutshopperBaseUrl;
    }

    @NonNull
    public String getInitiationUrl() {
        return mInitiationUrl;
    }

    @NonNull
    public String getDisableRecurringDetailUrl() {
        return mDisableRecurringDetailUrl;
    }

    @NonNull
    public HostProvider getLogoApiHostProvider() {
        return new LogoApiHostProvider(mCheckoutshopperBaseUrl);
    }

    @NonNull
    public PaymentSessionImpl copyByRemovingOneClickPaymentMethod(@NonNull PaymentMethodImpl oneClickPaymentMethod) {
        try {
            JSONObject oldJsonObject = getJsonObject();
            Iterator<String> keys = oldJsonObject.keys();

            JSONObject newJsonObject = new JSONObject();

            while (keys.hasNext()) {
                String key = keys.next();

                if (KEY_ONE_CLICK_PAYMENT_METHODS.equals(key)) {
                    JSONArray oldJsonArray = oldJsonObject.getJSONArray(key);
                    JSONArray newJsonArray = new JSONArray();

                    for (int i = 0; i < oldJsonArray.length(); i++) {
                        JSONObject jsonObject = oldJsonArray.getJSONObject(i);

                        if (!jsonObject.toString().equals(JsonObject.serialize(oneClickPaymentMethod).toString())) {
                            newJsonArray.put(jsonObject);
                        }
                    }

                    newJsonObject.put(key, newJsonArray);
                } else {
                    newJsonObject.put(key, oldJsonObject.get(key));
                }
            }

            return new PaymentSessionImpl(newJsonObject);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON.");
        }
    }

    private static final class LogoApiHostProvider implements HostProvider {
        private final String mUrl;

        private LogoApiHostProvider(@NonNull String checkoutshopperBaseUrl) {
            int endIndex = checkoutshopperBaseUrl.lastIndexOf("checkoutshopper");
            mUrl = checkoutshopperBaseUrl.substring(0, endIndex);
        }

        @NonNull
        @Override
        public String getUrl() {
            return mUrl;
        }
    }
}
