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

import com.adyen.checkout.base.model.payments.Amount;
import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class VoucherAction extends Action {
    @NonNull
    public static final Creator<VoucherAction> CREATOR = new Creator<>(VoucherAction.class);

    public static final String ACTION_TYPE = ActionTypes.VOUCHER;

    private static final String SURCHARGE = "surcharge";
    private static final String INITIAL_AMOUNT = "initialAmount";
    private static final String TOTAL_AMOUNT = "totalAmount";
    private static final String ISSUER = "issuer";
    private static final String EXPIRES_AT = "expiresAt";
    private static final String REFERENCE = "reference";
    private static final String ALTERNATIVE_REFERENCE = "alternativeReference";
    private static final String MERCHANT_NAME = "merchantName";

    @NonNull
    public static final Serializer<VoucherAction> SERIALIZER = new Serializer<VoucherAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull VoucherAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(SURCHARGE, ModelUtils.serializeOpt(modelObject.getSurcharge(), Amount.SERIALIZER));
                jsonObject.putOpt(INITIAL_AMOUNT, ModelUtils.serializeOpt(modelObject.getInitialAmount(), Amount.SERIALIZER));
                jsonObject.putOpt(TOTAL_AMOUNT, ModelUtils.serializeOpt(modelObject.getTotalAmount(), Amount.SERIALIZER));
                jsonObject.putOpt(ISSUER, modelObject.getIssuer());
                jsonObject.putOpt(EXPIRES_AT, modelObject.getExpiresAt());
                jsonObject.putOpt(REFERENCE, modelObject.getReference());
                jsonObject.putOpt(ALTERNATIVE_REFERENCE, modelObject.getAlternativeReference());
                jsonObject.putOpt(MERCHANT_NAME, modelObject.getMerchantName());
            } catch (JSONException e) {
                throw new ModelSerializationException(VoucherAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public VoucherAction deserialize(@NonNull JSONObject jsonObject) {
            final VoucherAction voucherAction = new VoucherAction();

            // getting parameters from parent class
            voucherAction.setType(jsonObject.optString(Action.TYPE, null));
            voucherAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            voucherAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            voucherAction.setSurcharge(ModelUtils.deserializeOpt(jsonObject.optJSONObject(SURCHARGE), Amount.SERIALIZER));
            voucherAction.setInitialAmount(ModelUtils.deserializeOpt(jsonObject.optJSONObject(INITIAL_AMOUNT), Amount.SERIALIZER));
            voucherAction.setTotalAmount(ModelUtils.deserializeOpt(jsonObject.optJSONObject(TOTAL_AMOUNT), Amount.SERIALIZER));
            voucherAction.setIssuer(jsonObject.optString(ISSUER));
            voucherAction.setExpiresAt(jsonObject.optString(EXPIRES_AT));
            voucherAction.setReference(jsonObject.optString(REFERENCE));
            voucherAction.setAlternativeReference(jsonObject.optString(ALTERNATIVE_REFERENCE));
            voucherAction.setMerchantName(jsonObject.optString(MERCHANT_NAME));
            return voucherAction;
        }
    };

    private Amount surcharge;
    private Amount initialAmount;
    private Amount totalAmount;
    private String issuer;
    private String expiresAt;
    private String reference;
    private String alternativeReference;
    private String merchantName;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public Amount getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(@Nullable Amount surcharge) {
        this.surcharge = surcharge;
    }

    @Nullable
    public Amount getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(@Nullable Amount initialAmount) {
        this.initialAmount = initialAmount;
    }

    @Nullable
    public Amount getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@Nullable Amount totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Nullable
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(@Nullable String issuer) {
        this.issuer = issuer;
    }

    @Nullable
    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(@Nullable String expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Nullable
    public String getReference() {
        return reference;
    }

    public void setReference(@Nullable String reference) {
        this.reference = reference;
    }

    @Nullable
    public String getAlternativeReference() {
        return alternativeReference;
    }

    public void setAlternativeReference(@Nullable String alternativeReference) {
        this.alternativeReference = alternativeReference;
    }

    @Nullable
    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(@Nullable String merchantName) {
        this.merchantName = merchantName;
    }
}
