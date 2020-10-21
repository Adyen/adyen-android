/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class QrCodeAction extends Action {
    @NonNull
    public static final Creator<QrCodeAction> CREATOR = new Creator<>(QrCodeAction.class);

    public static final String ACTION_TYPE = ActionTypes.QR_CODE;

    private static final String QR_CODE_DATA = "qrCodeData";

    @NonNull
    public static final Serializer<QrCodeAction> SERIALIZER = new Serializer<QrCodeAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull QrCodeAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(QR_CODE_DATA, modelObject.getQrCodeData());
            } catch (JSONException e) {
                throw new ModelSerializationException(QrCodeAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public QrCodeAction deserialize(@NonNull JSONObject jsonObject) {
            final QrCodeAction qrCodeAction = new QrCodeAction();

            // getting parameters from parent class
            qrCodeAction.setType(jsonObject.optString(Action.TYPE, null));
            qrCodeAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            qrCodeAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            qrCodeAction.setQrCodeData(jsonObject.optString(QR_CODE_DATA));
            return qrCodeAction;
        }
    };

    private String qrCodeData;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(@Nullable String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }
}
