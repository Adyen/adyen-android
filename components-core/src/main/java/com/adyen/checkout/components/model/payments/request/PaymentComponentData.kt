/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.components.model.payments.request;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.model.payments.Amount;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class PaymentComponentData<PaymentMethodDetailsT extends PaymentMethodDetails> extends ModelObject {

    @NonNull
    public static final Creator<PaymentComponentData> CREATOR = new Creator<>(PaymentComponentData.class);

    private static final String PAYMENT_METHOD = "paymentMethod";
    private static final String STORE_PAYMENT_METHOD = "storePaymentMethod";
    private static final String SHOPPER_REFERENCE = "shopperReference";
    private static final String AMOUNT = "amount";
    private static final String BILLING_ADDRESS = "billingAddress";
    private static final String DELIVERY_ADDRESS = "deliveryAddress";
    private static final String SHOPPER_NAME = "shopperName";
    private static final String TELEPHONE_NUMBER = "telephoneNumber";
    private static final String SHOPPER_EMAIL = "shopperEmail";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    private static final String SOCIAL_SECURITY_NUMBER = "socialSecurityNumber";
    private static final String INSTALLMENTS = "installments";
    private static final String ORDER = "order";

    @NonNull
    public static final Serializer<PaymentComponentData> SERIALIZER = new Serializer<PaymentComponentData>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull PaymentComponentData modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(PAYMENT_METHOD, ModelUtils.serializeOpt(modelObject.getPaymentMethod(), PaymentMethodDetails.SERIALIZER));
                jsonObject.putOpt(STORE_PAYMENT_METHOD, modelObject.isStorePaymentMethodEnable());
                jsonObject.putOpt(SHOPPER_REFERENCE, modelObject.getShopperReference());
                jsonObject.putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.getAmount(), Amount.SERIALIZER));
                jsonObject.putOpt(BILLING_ADDRESS, ModelUtils.serializeOpt(modelObject.getBillingAddress(), Address.SERIALIZER));
                jsonObject.putOpt(DELIVERY_ADDRESS, ModelUtils.serializeOpt(modelObject.getDeliveryAddress(), Address.SERIALIZER));
                jsonObject.putOpt(SHOPPER_NAME, ModelUtils.serializeOpt(modelObject.getShopperName(), ShopperName.SERIALIZER));
                jsonObject.putOpt(TELEPHONE_NUMBER, modelObject.getTelephoneNumber());
                jsonObject.putOpt(SHOPPER_EMAIL, modelObject.getShopperEmail());
                jsonObject.putOpt(DATE_OF_BIRTH, modelObject.getDateOfBirth());
                jsonObject.putOpt(SOCIAL_SECURITY_NUMBER, modelObject.getSocialSecurityNumber());
                jsonObject.putOpt(INSTALLMENTS, ModelUtils.serializeOpt(modelObject.getInstallments(), Installments.SERIALIZER));
                jsonObject.putOpt(ORDER, ModelUtils.serializeOpt(modelObject.getOrder(), OrderRequest.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentComponentData.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public PaymentComponentData deserialize(@NonNull JSONObject jsonObject) {
            final PaymentComponentData paymentComponentData = new PaymentComponentData();
            //noinspection unchecked
            paymentComponentData.setPaymentMethod(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(PAYMENT_METHOD), PaymentMethodDetails.SERIALIZER));
            paymentComponentData.setStorePaymentMethod(jsonObject.optBoolean(STORE_PAYMENT_METHOD));
            paymentComponentData.setShopperReference(jsonObject.optString(SHOPPER_REFERENCE));
            paymentComponentData.setAmount(ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER));
            paymentComponentData.setBillingAddress(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(BILLING_ADDRESS), Address.SERIALIZER));
            paymentComponentData.setDeliveryAddress(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(DELIVERY_ADDRESS), Address.SERIALIZER));
            paymentComponentData.setShopperName(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(SHOPPER_NAME), ShopperName.SERIALIZER));
            paymentComponentData.setTelephoneNumber(jsonObject.optString(TELEPHONE_NUMBER));
            paymentComponentData.setShopperEmail(jsonObject.optString(SHOPPER_EMAIL));
            paymentComponentData.setDateOfBirth(jsonObject.optString(DATE_OF_BIRTH));
            paymentComponentData.setSocialSecurityNumber(jsonObject.optString(SOCIAL_SECURITY_NUMBER));
            paymentComponentData.setInstallments(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(INSTALLMENTS), Installments.SERIALIZER)
            );
            paymentComponentData.setOrder(ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER));

            return paymentComponentData;
        }
    };

    private PaymentMethodDetailsT paymentMethod;
    private boolean storePaymentMethod;
    private String shopperReference;
    private Amount amount;
    private Address billingAddress;
    private Address deliveryAddress;
    private ShopperName shopperName;
    private String telephoneNumber;
    private String shopperEmail;
    private String dateOfBirth;
    private String socialSecurityNumber;
    private Installments installments;
    private OrderRequest order;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public PaymentMethodDetailsT getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@Nullable PaymentMethodDetailsT paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStorePaymentMethod(boolean status) {
        storePaymentMethod = status;
    }

    public boolean isStorePaymentMethodEnable() {
        return storePaymentMethod;
    }

    public void setShopperReference(@Nullable String shopperReference) {
        this.shopperReference = shopperReference;
    }

    @Nullable
    public String getShopperReference() {
        return shopperReference;
    }

    @Nullable
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(@Nullable Amount amount) {
        this.amount = amount;
    }

    @Nullable
    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(@Nullable Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    @Nullable
    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(@Nullable Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @Nullable
    public ShopperName getShopperName() {
        return shopperName;
    }

    public void setShopperName(@Nullable ShopperName shopperName) {
        this.shopperName = shopperName;
    }

    @Nullable
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(@Nullable String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    @Nullable
    public String getShopperEmail() {
        return shopperEmail;
    }

    public void setShopperEmail(@Nullable String shopperEmail) {
        this.shopperEmail = shopperEmail;
    }

    @Nullable
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@Nullable String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Nullable
    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(@Nullable String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    @Nullable
    public Installments getInstallments() {
        return installments;
    }

    public void setInstallments(@Nullable Installments installments) {
        this.installments = installments;
    }

    @Nullable
    public OrderRequest getOrder() {
        return order;
    }

    public void setOrder(@Nullable OrderRequest order) {
        this.order = order;
    }
}
