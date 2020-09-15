/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/7/2019.
 */

package com.adyen.checkout.googlepay.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.AvoidFieldNameMatchingTypeName", "PMD.DataClass"})
public class TokenizationParameters extends ModelObject {

    @NonNull
    public static final Creator<TokenizationParameters> CREATOR = new Creator<>(TokenizationParameters.class);

    private static final String GATEWAY = "gateway";
    private static final String GATEWAY_MERCHANT_ID = "gatewayMerchantId";

    @NonNull
    public static final Serializer<TokenizationParameters> SERIALIZER = new Serializer<TokenizationParameters>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull TokenizationParameters modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(GATEWAY, modelObject.getGateway());
                jsonObject.putOpt(GATEWAY_MERCHANT_ID, modelObject.getGatewayMerchantId());
            } catch (JSONException e) {
                throw new ModelSerializationException(TokenizationParameters.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public TokenizationParameters deserialize(@NonNull JSONObject jsonObject) {
            final TokenizationParameters tokenizationParameters = new TokenizationParameters();
            tokenizationParameters.setGateway(jsonObject.optString(GATEWAY, null));
            tokenizationParameters.setGatewayMerchantId(jsonObject.optString(GATEWAY_MERCHANT_ID, null));
            return tokenizationParameters;
        }
    };

    private String gateway;
    private String gatewayMerchantId;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getGateway() {
        return gateway;
    }

    public void setGateway(@Nullable String gateway) {
        this.gateway = gateway;
    }

    @Nullable
    public String getGatewayMerchantId() {
        return gatewayMerchantId;
    }

    public void setGatewayMerchantId(@Nullable String gatewayMerchantId) {
        this.gatewayMerchantId = gatewayMerchantId;
    }
}
