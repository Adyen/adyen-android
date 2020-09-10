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
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @deprecated Deprecated in favor of {@link SdkAction}
 */
@Deprecated
@SuppressWarnings({"MemberName"})
public class WeChatPaySdkAction extends Action {
    @NonNull
    public static final Creator<WeChatPaySdkAction> CREATOR = new Creator<>(WeChatPaySdkAction.class);

    public static final String ACTION_TYPE = "wechatpaySDK";

    private static final String SDK_DATA = "sdkData";

    @NonNull
    public static final Serializer<WeChatPaySdkAction> SERIALIZER = new Serializer<WeChatPaySdkAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull WeChatPaySdkAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(SDK_DATA, ModelUtils.serializeOpt(modelObject.getSdkData(), WeChatPaySdkData.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(WeChatPaySdkAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public WeChatPaySdkAction deserialize(@NonNull JSONObject jsonObject) {
            final WeChatPaySdkAction weChatPaySdkAction = new WeChatPaySdkAction();

            // getting parameters from parent class
            weChatPaySdkAction.setType(jsonObject.optString(Action.TYPE, null));
            weChatPaySdkAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            weChatPaySdkAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            weChatPaySdkAction.setSdkData(ModelUtils.deserializeOpt(jsonObject.optJSONObject(SDK_DATA), WeChatPaySdkData.SERIALIZER));
            return weChatPaySdkAction;
        }
    };

    private WeChatPaySdkData sdkData;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public WeChatPaySdkData getSdkData() {
        return sdkData;
    }

    public void setSdkData(@Nullable WeChatPaySdkData sdkData) {
        this.sdkData = sdkData;
    }
}
