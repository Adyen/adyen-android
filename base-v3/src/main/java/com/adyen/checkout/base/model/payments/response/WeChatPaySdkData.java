/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/10/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class WeChatPaySdkData extends SdkData {
    @NonNull
    public static final Creator<WeChatPaySdkData> CREATOR = new Creator<>(WeChatPaySdkData.class);

    private static final String APP_ID = "appid";
    private static final String NONCE_STR = "noncestr";
    private static final String PACKAGE_VALUE = "packageValue";
    private static final String PARTNER_ID = "partnerid";
    private static final String PREPAY_ID = "prepayid";
    private static final String SIGN = "sign";
    private static final String TIMESTAMP = "timestamp";

    @NonNull
    public static final Serializer<WeChatPaySdkData> SERIALIZER = new Serializer<WeChatPaySdkData>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull WeChatPaySdkData modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(APP_ID, modelObject.getAppid());
                jsonObject.putOpt(NONCE_STR, modelObject.getNoncestr());
                jsonObject.putOpt(PACKAGE_VALUE, modelObject.getPackageValue());
                jsonObject.putOpt(PARTNER_ID, modelObject.getPartnerid());
                jsonObject.putOpt(PREPAY_ID, modelObject.getPrepayid());
                jsonObject.putOpt(SIGN, modelObject.getSign());
                jsonObject.putOpt(TIMESTAMP, modelObject.getTimestamp());
            } catch (JSONException e) {
                throw new ModelSerializationException(WeChatPaySdkData.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public WeChatPaySdkData deserialize(@NonNull JSONObject jsonObject) {
            final WeChatPaySdkData weChatPaySdkData = new WeChatPaySdkData();
            weChatPaySdkData.setAppid(jsonObject.optString(APP_ID, null));
            weChatPaySdkData.setNoncestr(jsonObject.optString(NONCE_STR, null));
            weChatPaySdkData.setPackageValue(jsonObject.optString(PACKAGE_VALUE, null));
            weChatPaySdkData.setPartnerid(jsonObject.optString(PARTNER_ID, null));
            weChatPaySdkData.setPrepayid(jsonObject.optString(PREPAY_ID, null));
            weChatPaySdkData.setSign(jsonObject.optString(SIGN, null));
            weChatPaySdkData.setTimestamp(jsonObject.optString(TIMESTAMP, null));

            return weChatPaySdkData;
        }
    };

    private String appid;
    private String noncestr;
    private String packageValue;
    private String partnerid;
    private String prepayid;
    private String sign;
    private String timestamp;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getAppid() {
        return appid;
    }

    public void setAppid(@Nullable String appid) {
        this.appid = appid;
    }

    @Nullable
    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(@Nullable String noncestr) {
        this.noncestr = noncestr;
    }

    @Nullable
    public String getPackageValue() {
        return packageValue;
    }

    public void setPackageValue(@Nullable String packageValue) {
        this.packageValue = packageValue;
    }

    @Nullable
    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(@Nullable String partnerid) {
        this.partnerid = partnerid;
    }

    @Nullable
    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(@Nullable String prepayid) {
        this.prepayid = prepayid;
    }

    @Nullable
    public String getSign() {
        return sign;
    }

    public void setSign(@Nullable String sign) {
        this.sign = sign;
    }

    @Nullable
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@Nullable String timestamp) {
        this.timestamp = timestamp;
    }
}
