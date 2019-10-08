/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */

package com.adyen.checkout.googlepay.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class TransactionInfoModel extends ModelObject {

    @NonNull
    public static final Creator<TransactionInfoModel> CREATOR = new Creator<>(TransactionInfoModel.class);

    private static final String CURRENCY_CODE = "currencyCode";
    private static final String TOTAL_PRICE_STATUS = "totalPriceStatus";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String TOTAL_PRICE_LABEL = "totalPriceLabel";
    private static final String CHECKOUT_OPTION = "checkoutOption";

    @NonNull
    public static final Serializer<TransactionInfoModel> SERIALIZER = new Serializer<TransactionInfoModel>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull TransactionInfoModel modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CURRENCY_CODE, modelObject.getCurrencyCode());
                jsonObject.putOpt(TOTAL_PRICE_STATUS, modelObject.getTotalPriceStatus());
                jsonObject.putOpt(TOTAL_PRICE, modelObject.getTotalPrice());
                jsonObject.putOpt(TOTAL_PRICE_LABEL, modelObject.getTotalPriceLabel());
                jsonObject.putOpt(CHECKOUT_OPTION, modelObject.getCheckoutOption());
            } catch (JSONException e) {
                throw new ModelSerializationException(TransactionInfoModel.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public TransactionInfoModel deserialize(@NonNull JSONObject jsonObject) {
            final TransactionInfoModel transactionInfoModel = new TransactionInfoModel();
            transactionInfoModel.setCurrencyCode(jsonObject.optString(CURRENCY_CODE, null));
            transactionInfoModel.setTotalPriceStatus(jsonObject.optString(TOTAL_PRICE_STATUS, null));
            transactionInfoModel.setTotalPrice(jsonObject.optString(TOTAL_PRICE, null));
            transactionInfoModel.setTotalPriceLabel(jsonObject.optString(TOTAL_PRICE_LABEL, null));
            transactionInfoModel.setCheckoutOption(jsonObject.optString(CHECKOUT_OPTION, null));
            return transactionInfoModel;
        }
    };

    private String currencyCode;
    private String totalPriceStatus;
    private String totalPrice;
    private String totalPriceLabel;
    private String checkoutOption;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(@Nullable String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Nullable
    public String getTotalPriceStatus() {
        return totalPriceStatus;
    }

    public void setTotalPriceStatus(@Nullable String totalPriceStatus) {
        this.totalPriceStatus = totalPriceStatus;
    }

    @Nullable
    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(@Nullable String totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Nullable
    public String getTotalPriceLabel() {
        return totalPriceLabel;
    }

    public void setTotalPriceLabel(@Nullable String totalPriceLabel) {
        this.totalPriceLabel = totalPriceLabel;
    }

    @Nullable
    public String getCheckoutOption() {
        return checkoutOption;
    }

    public void setCheckoutOption(@Nullable String checkoutOption) {
        this.checkoutOption = checkoutOption;
    }
}
