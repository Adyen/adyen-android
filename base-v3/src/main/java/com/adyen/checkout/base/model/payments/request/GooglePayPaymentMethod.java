/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class GooglePayPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<GooglePayPaymentMethod> CREATOR = new Creator<>(GooglePayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.GOOGLE_PAY;

    private static final String GOOGLE_PAY_TOKEN = "googlePayToken";
    private static final String GOOGLE_PAY_CARD_NETWORK = "googlePayCardNetwork";

    @NonNull
    public static final Serializer<GooglePayPaymentMethod> SERIALIZER = new Serializer<GooglePayPaymentMethod>() {
        @NonNull
        @Override
        public JSONObject serialize(@NonNull GooglePayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(GOOGLE_PAY_TOKEN, modelObject.getGooglePayToken());
                jsonObject.putOpt(GOOGLE_PAY_CARD_NETWORK, modelObject.getGooglePayCardNetwork());
            } catch (JSONException e) {
                throw new ModelSerializationException(GooglePayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public GooglePayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final GooglePayPaymentMethod googlePayPaymentMethod = new GooglePayPaymentMethod();

            // getting parameters from parent class
            googlePayPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            googlePayPaymentMethod.setGooglePayToken(jsonObject.optString(GOOGLE_PAY_TOKEN, null));
            googlePayPaymentMethod.setGooglePayCardNetwork(jsonObject.optString(GOOGLE_PAY_CARD_NETWORK, null));

            return googlePayPaymentMethod;
        }
    };

    private String googlePayToken;
    private String googlePayCardNetwork;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    /**
     * @deprecated Deprecated in favor of {@link #getGooglePayToken()}.
     * @return The token returned from Google Pay
     */
    @Deprecated
    @Nullable
    public String getToken() {
        return googlePayToken;
    }

    /**
     * @deprecated Deprecated in favor of {@link #setGooglePayToken(String)}.
     * @param token The token returned from Google Pay
     */
    @Deprecated
    public void setToken(@Nullable String token) {
        googlePayToken = token;
    }

    @Nullable
    public String getGooglePayToken() {
        return googlePayToken;
    }

    public void setGooglePayToken(@Nullable String googlePayToken) {
        this.googlePayToken = googlePayToken;
    }

    @Nullable
    public String getGooglePayCardNetwork() {
        return googlePayCardNetwork;
    }

    public void setGooglePayCardNetwork(@Nullable String googlePayCardNetwork) {
        this.googlePayCardNetwork = googlePayCardNetwork;
    }
}
