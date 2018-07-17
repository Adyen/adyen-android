package com.adyen.checkout.core.internal.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 28/12/2017.
 */
public final class PaymentSessionResponse extends JsonObject {
    private static final Creator<PaymentSessionResponse> CREATOR = new DefaultCreator<>(PaymentSessionResponse.class);

    private final String mPaymentSession;

    private PaymentSessionResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mPaymentSession = jsonObject.getString("paymentSession");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentSessionResponse that = (PaymentSessionResponse) o;

        return mPaymentSession != null ? mPaymentSession.equals(that.mPaymentSession) : that.mPaymentSession == null;
    }

    @Override
    public int hashCode() {
        return mPaymentSession != null ? mPaymentSession.hashCode() : 0;
    }

    @NonNull
    public String getPaymentSession() {
        return mPaymentSession;
    }
}
