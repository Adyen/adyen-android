/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2020.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class SdkAction<SdkDataT extends SdkData> extends Action {
    @NonNull
    public static final Creator<SdkAction> CREATOR = new Creator<>(SdkAction.class);

    public static final String ACTION_TYPE = ActionTypes.SDK;

    private static final String SDK_DATA = "sdkData";

    @NonNull
    public static final Serializer<SdkAction> SERIALIZER = new Serializer<SdkAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull SdkAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                //noinspection unchecked
                final Serializer<SdkData> serializer = (Serializer<SdkData>) getSdkDataSerializer(modelObject.getPaymentMethodType());
                if (modelObject.getSdkData() != null) {
                    jsonObject.putOpt(SDK_DATA, ModelUtils.serializeOpt(modelObject.getSdkData(), serializer));
                }
            } catch (JSONException e) {
                throw new ModelSerializationException(WeChatPaySdkAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public SdkAction deserialize(@NonNull JSONObject jsonObject) {
            final SdkAction sdkAction = new SdkAction();

            // getting parameters from parent class
            sdkAction.setType(jsonObject.optString(Action.TYPE, null));
            sdkAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            sdkAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            final Serializer<? extends  SdkData> serializer = getSdkDataSerializer(sdkAction.getPaymentMethodType());
            //noinspection unchecked
            sdkAction.setSdkData(ModelUtils.deserializeOpt(jsonObject.optJSONObject(SDK_DATA), serializer));

            return sdkAction;
        }

        @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
        @NonNull
        private Serializer<? extends SdkData> getSdkDataSerializer(@Nullable String paymentMethodType) {
            if (paymentMethodType == null) {
                throw new CheckoutException("SdkAction cannot be parsed with null paymentMethodType.");
            }

            //noinspection SwitchStatementWithTooFewBranches
            switch (paymentMethodType) {
                case PaymentMethodTypes.WECHAT_PAY_SDK:
                    return WeChatPaySdkData.SERIALIZER;
                default:
                    throw new CheckoutException("sdkData not found for type paymentMethodType - " + paymentMethodType);
            }
        }

    };

    private SdkDataT sdkData;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public SdkDataT getSdkData() {
        return sdkData;
    }

    public void setSdkData(@Nullable SdkDataT sdkData) {
        this.sdkData = sdkData;
    }
}
