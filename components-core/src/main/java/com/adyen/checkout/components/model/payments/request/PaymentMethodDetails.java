/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/5/2019.
 */

package com.adyen.checkout.components.model.payments.request;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONObject;

/**
 * This class is a top level abstraction for data objects that can be serialized to the paymentMethod parameter on a payments/ call.
 * The {@link #SERIALIZER} object can serialize this to a {@link JSONObject} with the corresponding data.
 *
 * <p/>
 * Alternatively you can use other parsing libraries if they support polymorphism.
 */
@SuppressWarnings({"MemberName", "PMD.DataClass"})
public abstract class PaymentMethodDetails extends ModelObject {

    public static final String TYPE = "type";

    @NonNull
    public static final Serializer<PaymentMethodDetails> SERIALIZER = new Serializer<PaymentMethodDetails>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull PaymentMethodDetails modelObject) {
            final String paymentMethodType = modelObject.getType();
            if (TextUtils.isEmpty(paymentMethodType)) {
                throw new CheckoutException("PaymentMethod type not found");
            }
            //noinspection unchecked
            final Serializer<PaymentMethodDetails> serializer = (Serializer<PaymentMethodDetails>) getChildSerializer(paymentMethodType);
            return serializer.serialize(modelObject);
        }

        @NonNull
        @Override
        public PaymentMethodDetails deserialize(@NonNull JSONObject jsonObject) {
            final String actionType = jsonObject.optString(TYPE, null);
            if (TextUtils.isEmpty(actionType)) {
                throw new CheckoutException("PaymentMethod type not found");
            }
            //noinspection unchecked
            final Serializer<PaymentMethodDetails> serializer = (Serializer<PaymentMethodDetails>) getChildSerializer(actionType);
            return serializer.deserialize(jsonObject);
        }
    };

    private String type;

    @NonNull
    static Serializer<? extends PaymentMethodDetails> getChildSerializer(@NonNull String paymentMethodType) {
        switch (paymentMethodType) {
            case IdealPaymentMethod.PAYMENT_METHOD_TYPE:
                return IdealPaymentMethod.SERIALIZER;
            case CardPaymentMethod.PAYMENT_METHOD_TYPE:
                return CardPaymentMethod.SERIALIZER;
            //Intentional fallthrough of different flavors of Molpay
            case PaymentMethodTypes.MOLPAY_MALAYSIA:
            case PaymentMethodTypes.MOLPAY_THAILAND:
            case PaymentMethodTypes.MOLPAY_VIETNAM:
                return MolpayPaymentMethod.SERIALIZER;
            case DotpayPaymentMethod.PAYMENT_METHOD_TYPE:
                return DotpayPaymentMethod.SERIALIZER;
            case EPSPaymentMethod.PAYMENT_METHOD_TYPE:
                return EPSPaymentMethod.SERIALIZER;
            case OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE:
                return OpenBankingPaymentMethod.SERIALIZER;
            case EntercashPaymentMethod.PAYMENT_METHOD_TYPE:
                return EntercashPaymentMethod.SERIALIZER;
            case GiftCardPaymentMethod.PAYMENT_METHOD_TYPE:
                return GiftCardPaymentMethod.SERIALIZER;
            //Intentional fallthrough of the new and legacy google pay txVariants
            case PaymentMethodTypes.GOOGLE_PAY:
            case PaymentMethodTypes.GOOGLE_PAY_LEGACY:
                return GooglePayPaymentMethod.SERIALIZER;
            case SepaPaymentMethod.PAYMENT_METHOD_TYPE:
                return SepaPaymentMethod.SERIALIZER;
            case MBWayPaymentMethod.PAYMENT_METHOD_TYPE:
                return MBWayPaymentMethod.SERIALIZER;
            case BlikPaymentMethod.PAYMENT_METHOD_TYPE:
                return BlikPaymentMethod.SERIALIZER;
            case BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE:
                return BacsDirectDebitPaymentMethod.SERIALIZER;
            default:
                return GenericPaymentMethod.SERIALIZER;
        }
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }
}
