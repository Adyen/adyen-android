/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */

package com.adyen.checkout.components.model.paymentmethods;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class Configuration extends ModelObject {

    @NonNull
    public static final Creator<Configuration> CREATOR = new Creator<>(Configuration.class);

    // Google Pay
    private static final String MERCHANT_ID = "merchantId";
    private static final String GATEWAY_MERCHANT_ID = "gatewayMerchantId";
    // PayPal
    private static final String INTENT = "intent";
    // Card
    private static final String KOREAN_AUTHENTICATION_REQUIRED = "koreanAuthenticationRequired";



    @NonNull
    public static final Serializer<Configuration> SERIALIZER = new Serializer<Configuration>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Configuration modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(MERCHANT_ID, modelObject.getMerchantId());
                jsonObject.putOpt(GATEWAY_MERCHANT_ID, modelObject.getGatewayMerchantId());
                jsonObject.putOpt(INTENT, modelObject.getIntent());
                jsonObject.putOpt(KOREAN_AUTHENTICATION_REQUIRED, modelObject.getKoreanAuthenticationRequired());
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethod.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Configuration deserialize(@NonNull JSONObject jsonObject) {
            final Configuration configuration = new Configuration();
            configuration.setMerchantId(jsonObject.optString(MERCHANT_ID, null));
            configuration.setGatewayMerchantId(jsonObject.optString(GATEWAY_MERCHANT_ID, null));
            configuration.setIntent(jsonObject.optString(INTENT, null));
            configuration.setKoreanAuthenticationRequired(jsonObject.optString(KOREAN_AUTHENTICATION_REQUIRED, null));
            return configuration;
        }
    };

    private String merchantId;
    private String gatewayMerchantId;
    private String intent;
    private String koreanAuthenticationRequired;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getMerchantId() {
        return merchantId;
    }

    @Nullable
    public String getGatewayMerchantId() {
        return gatewayMerchantId;
    }

    @Nullable
    public String getIntent() {
        return intent;
    }

    @Nullable
    public String getKoreanAuthenticationRequired() {
        return koreanAuthenticationRequired;
    }

    public void setMerchantId(@Nullable String merchantId) {
        this.merchantId = merchantId;
    }

    public void setGatewayMerchantId(@Nullable String gatewayMerchantId) {
        this.gatewayMerchantId = gatewayMerchantId;
    }

    public void setIntent(@Nullable String intent) {
        this.intent = intent;
    }

    public void setKoreanAuthenticationRequired(@Nullable String koreanAuthenticationRequired) {
        this.koreanAuthenticationRequired = koreanAuthenticationRequired;
    }
}
