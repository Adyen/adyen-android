/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONObject;

/**
 * An Action is an object from the response of the /payments endpoint that indicates what needs to be done to continue the payment.
 * Each type of Action contains different properties, so we use polymorphism to parse which type of Action we are dealing with.
 */
@SuppressWarnings({"MemberName", "PMD.DataClass"})
public abstract class Action extends ModelObject {

    public static final String TYPE = "type";
    public static final String PAYMENT_DATA = "paymentData";
    public static final String PAYMENT_METHOD_TYPE = "paymentMethodType";

    @NonNull
    public static final Serializer<Action> SERIALIZER = new Serializer<Action>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Action modelObject) {
            final String actionType = modelObject.getType();
            if (TextUtils.isEmpty(actionType)) {
                throw new CheckoutException("Action type not found");
            }
            //noinspection unchecked
            final Serializer<Action> serializer = (Serializer<Action>) getChildSerializer(actionType);
            return serializer.serialize(modelObject);
        }

        @NonNull
        @Override
        public Action deserialize(@NonNull JSONObject jsonObject) {
            final String actionType = jsonObject.optString(TYPE);
            if (TextUtils.isEmpty(actionType)) {
                throw new CheckoutException("Action type not found");
            }
            //noinspection unchecked
            final Serializer<Action> serializer = (Serializer<Action>) getChildSerializer(actionType);
            return serializer.deserialize(jsonObject);
        }
    };

    private String type;
    private String paymentData;
    private String paymentMethodType;

    @SuppressWarnings(Lint.SYNTHETIC)
    @NonNull
    static Serializer<? extends Action> getChildSerializer(@NonNull String actionType) {
        switch (actionType) {
            case RedirectAction.ACTION_TYPE:
                return RedirectAction.SERIALIZER;
            case Threeds2FingerprintAction.ACTION_TYPE:
                return Threeds2FingerprintAction.SERIALIZER;
            case Threeds2ChallengeAction.ACTION_TYPE:
                return Threeds2ChallengeAction.SERIALIZER;
            case QrCodeAction.ACTION_TYPE:
                return QrCodeAction.SERIALIZER;
            case VoucherAction.ACTION_TYPE:
                return VoucherAction.SERIALIZER;
            case WeChatPaySdkAction.ACTION_TYPE:
                return WeChatPaySdkAction.SERIALIZER;
            case SdkAction.ACTION_TYPE:
                return SdkAction.SERIALIZER;
            case AwaitAction.ACTION_TYPE:
                return AwaitAction.SERIALIZER;
            default:
                throw new CheckoutException("Action type not found - " + actionType);
        }
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(@Nullable String paymentData) {
        this.paymentData = paymentData;
    }

    @Nullable
    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public void setPaymentMethodType(@Nullable String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }
}
