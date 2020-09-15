/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
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

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class MerchantInfo extends ModelObject {

    @NonNull
    public static final Creator<MerchantInfo> CREATOR = new Creator<>(MerchantInfo.class);

    private static final String MERCHANT_NAME = "merchantName";
    private static final String MERCHANT_ID = "merchantId";

    @NonNull
    public static final Serializer<MerchantInfo> SERIALIZER = new Serializer<MerchantInfo>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull MerchantInfo modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(MERCHANT_NAME, modelObject.getMerchantName());
                jsonObject.putOpt(MERCHANT_ID, modelObject.getMerchantId());

            } catch (JSONException e) {
                throw new ModelSerializationException(MerchantInfo.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public MerchantInfo deserialize(@NonNull JSONObject jsonObject) {
            final MerchantInfo merchantInfo = new MerchantInfo();
            merchantInfo.setMerchantName(jsonObject.optString(MERCHANT_NAME, null));
            merchantInfo.setMerchantId(jsonObject.optString(MERCHANT_ID, null));
            return merchantInfo;
        }
    };

    private String merchantName;
    private String merchantId;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(@Nullable String merchantName) {
        this.merchantName = merchantName;
    }

    @Nullable
    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(@Nullable String merchantId) {
        this.merchantId = merchantId;
    }
}
