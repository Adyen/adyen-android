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
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class PaymentDataRequestModel extends ModelObject {

    @NonNull
    public static final Creator<PaymentDataRequestModel> CREATOR = new Creator<>(PaymentDataRequestModel.class);

    private static final String API_VERSION = "apiVersion";
    private static final String API_VERSION_MINOR = "apiVersionMinor";
    private static final String MERCHANT_INFO = "merchantInfo";
    private static final String ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods";
    private static final String TRANSACTION_INFO = "transactionInfo";
    private static final String EMAIL_REQUIRED = "emailRequired";
    private static final String SHIPPING_ADDRESS_REQUIRED = "shippingAddressRequired";
    private static final String SHIPPING_ADDRESS_PARAMETERS = "shippingAddressParameters";


    @NonNull
    public static final Serializer<PaymentDataRequestModel> SERIALIZER = new Serializer<PaymentDataRequestModel>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull PaymentDataRequestModel modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(API_VERSION, modelObject.getApiVersion());
                jsonObject.putOpt(API_VERSION_MINOR, modelObject.getApiVersionMinor());
                jsonObject.putOpt(MERCHANT_INFO, ModelUtils.serializeOpt(modelObject.getMerchantInfo(), MerchantInfo.SERIALIZER));
                jsonObject.putOpt(ALLOWED_PAYMENT_METHODS,
                        ModelUtils.serializeOptList(modelObject.getAllowedPaymentMethods(), GooglePayPaymentMethodModel.SERIALIZER));
                jsonObject.putOpt(TRANSACTION_INFO, ModelUtils.serializeOpt(modelObject.getTransactionInfo(), TransactionInfoModel.SERIALIZER));
                jsonObject.putOpt(EMAIL_REQUIRED, modelObject.isEmailRequired());
                jsonObject.putOpt(SHIPPING_ADDRESS_REQUIRED, modelObject.isShippingAddressRequired());
                jsonObject.putOpt(SHIPPING_ADDRESS_PARAMETERS,
                        ModelUtils.serializeOpt(modelObject.getShippingAddressParameters(), ShippingAddressParameters.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentDataRequestModel.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public PaymentDataRequestModel deserialize(@NonNull JSONObject jsonObject) {
            final PaymentDataRequestModel paymentDataRequestModel = new PaymentDataRequestModel();
            paymentDataRequestModel.setApiVersion(jsonObject.optInt(API_VERSION));
            paymentDataRequestModel.setApiVersionMinor(jsonObject.optInt(API_VERSION_MINOR));
            paymentDataRequestModel.setMerchantInfo(ModelUtils.deserializeOpt(jsonObject.optJSONObject(MERCHANT_INFO), MerchantInfo.SERIALIZER));
            paymentDataRequestModel.setAllowedPaymentMethods(
                    ModelUtils.deserializeOptList(jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS), GooglePayPaymentMethodModel.SERIALIZER));
            paymentDataRequestModel.setTransactionInfo(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(TRANSACTION_INFO), TransactionInfoModel.SERIALIZER));
            paymentDataRequestModel.setEmailRequired(jsonObject.optBoolean(EMAIL_REQUIRED));
            paymentDataRequestModel.setShippingAddressRequired(jsonObject.optBoolean(SHIPPING_ADDRESS_REQUIRED));
            paymentDataRequestModel.setShippingAddressParameters(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(SHIPPING_ADDRESS_PARAMETERS), ShippingAddressParameters.SERIALIZER));
            return paymentDataRequestModel;
        }
    };

    private int apiVersion;
    private int apiVersionMinor;
    private MerchantInfo merchantInfo;
    private List<GooglePayPaymentMethodModel> allowedPaymentMethods;
    private TransactionInfoModel transactionInfo;
    private boolean emailRequired;
    private boolean shippingAddressRequired;
    private ShippingAddressParameters shippingAddressParameters;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getApiVersionMinor() {
        return apiVersionMinor;
    }

    public void setApiVersionMinor(int apiVersionMinor) {
        this.apiVersionMinor = apiVersionMinor;
    }

    @Nullable
    public MerchantInfo getMerchantInfo() {
        return merchantInfo;
    }

    public void setMerchantInfo(@Nullable MerchantInfo merchantInfo) {
        this.merchantInfo = merchantInfo;
    }

    @Nullable
    public List<GooglePayPaymentMethodModel> getAllowedPaymentMethods() {
        return allowedPaymentMethods;
    }

    public void setAllowedPaymentMethods(@Nullable List<GooglePayPaymentMethodModel> allowedPaymentMethods) {
        this.allowedPaymentMethods = allowedPaymentMethods;
    }

    @Nullable
    public TransactionInfoModel getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(@Nullable TransactionInfoModel transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public boolean isEmailRequired() {
        return emailRequired;
    }

    public void setEmailRequired(boolean emailRequired) {
        this.emailRequired = emailRequired;
    }

    public boolean isShippingAddressRequired() {
        return shippingAddressRequired;
    }

    public void setShippingAddressRequired(boolean shippingAddressRequired) {
        this.shippingAddressRequired = shippingAddressRequired;
    }

    @Nullable
    public ShippingAddressParameters getShippingAddressParameters() {
        return shippingAddressParameters;
    }

    public void setShippingAddressParameters(@Nullable ShippingAddressParameters shippingAddressParameters) {
        this.shippingAddressParameters = shippingAddressParameters;
    }
}
